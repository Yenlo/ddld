/**
 * Notifications module implementing also an http interceptor which passes every error to the module's notification service.
 */
angular.module('notifications', [], function ($provide, $httpProvider) {

    $provide.factory('notificationHttpInterceptor', function($q, notification) {

        var connection = true;
        var connected = function(newConnection){
            if(newConnection != connection){
                if(newConnection){
                    notification.info("Reconnected.");
                }
                else{
                    notification.warn("Connection lost.");
                }
                connection = newConnection;
            }
        };

        return {
            'response': function(response) {
                connected(true);
                return response || $q.when(response);
            },
            'responseError': function(rejection) {
	                connected(rejection.status != 404 || rejection.data.length != 0);
	                if(connection){
	                	if(rejection.data.identifier == 'REQUIRES_LOGIN'){
	                	    window.location = '/app/login';
	    	                return $q.reject(rejection);
	                	}
	                	else {
	                		if(rejection.data.message && rejection.data.message.length > 0){
			                    if(rejection.status < 500){
			                        notification.warn(rejection.data.message);
			                    }
			                    else{
			                        notification.danger('Er is een technische fout opgetreden. Mail naar support@yenlo.nl hoe je de fout kunt herproduceren wanneer je denkt dat dit een bug in het systeem is.');
			                    }
	                		}
	                	}
	                }
	                return $q.reject(rejection);
            }
        };

    });

    $httpProvider.interceptors.push('notificationHttpInterceptor');

});

/**
 * Listens for notification name messages, listing anything incoming as an alert.
 *
 * Level property can be used to indicate minimum level. 0: info, 1: success, 2: warning, 3: danger.
 */
angular.module('notifications').directive('alertListener', function(){
    return {
        restrict: 'A',
        controller: function($scope, notification) {

            $scope.notes = [];
            $scope.level = $scope.level || 2;

            var listen = function(level, name, type){
                if($scope.level <= level){
                    $scope.$on(name, function(scope, args) {
                        var content = Array.prototype.slice.call(args).join(", ");
                        $scope.notes.push({
                            type: type,
                            content: content
                        });
                    });
                }
            };

            listen(0, notification.nameInfo, 'info');
            listen(1, notification.nameSuccess, 'success');
            listen(2, notification.nameWarn, 'warning');
            listen(3, notification.nameDanger, 'danger');

            $scope.remove = function(index){
                $scope.notes.splice(index, 1);
            };

        },
        template: '<div data-ng-repeat="note in notes" data-alert type="note.type" close="remove($index)">'
            +   '{{ note.content }}'
            + '</div>',
        scope: {
            level: '@'
        }
    };
});

/**
 * Basically a service that accepts any messages and broadcasts them onto $rootScope.
 */
angular.module('notifications').service('notification', function($rootScope) {

    var self = this;

    self.nameSuccess = 'note:success';
    self.nameInfo = 'note:info';
    self.nameWarn = 'note:warn';
    self.nameDanger = 'note:danger';

    self.info = function(){
        $rootScope.$broadcast(self.nameInfo, arguments);
        console.log.apply(console, arguments);
    };

    self.success = function(){
        $rootScope.$broadcast(self.nameSuccess, arguments);
        console.log.apply(console, arguments);
    };

    self.warn = function(){
        $rootScope.$broadcast(self.nameWarn, arguments);
        console.warn.apply(console, arguments);
    };

    self.danger = function(){
        $rootScope.$broadcast(self.nameDanger, arguments);
        console.error.apply(console, arguments);
    };

    return self;

});