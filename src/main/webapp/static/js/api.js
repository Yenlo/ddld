/**
 * 'ddld.api' module definition, we make use of the following: - ngResource from
 * angular-resource js, for making use of the REST api client generators.
 */
angular.module('ddld.api', ['ngResource', 'SkP'], function ($provide, $httpProvider) {
  
  $provide.factory('apiHttpInterceptor', function ($q ,$injector) {
	return {
	  request: function (config) {
		var SessionService = $injector.get('SessionService');
	    var basic = SessionService.getBasic();
	    if(basic){
		    config.headers = config.headers || {};
		    config.headers['Authorization'] = basic;
	    }
	    return config;
	  }
	};
  });

  $httpProvider.interceptors.push('apiHttpInterceptor');

});

/**
 * client to /api/session
 * 
 * this is a service as there is only one user at a time, so the instance
 * containing the current session is shared amongst all service consumers.
 */
angular.module('ddld.api').service('SessionService', ['$http', '$rootScope', 'Store', function($http, $rootScope, Store) {

	var config = Store.mirror($rootScope, 'SessionServiceConfig', {
		identifier: null,
		current: null,
		password: null
	});

	this.getBasic = function(){
		if(config.identifier != null && config.password != null){
			return 'Basic ' + window.btoa(config.identifier + ':' + config.password);
		}
		else{
			return null;
		}
	};

	this.getIdentifier = function(){
		return config.identifier;
	};

	/**
	 * logs in as a user, returns the result of $http.post(..).
	 */
	this.login = function(identifier, password){
		return $http.post('/api/session/login', {
			email: identifier,
			password: password
	    }).success(function(response) {
	    	config.password = password;
	    	config.identifier = identifier;
	    	window.location = '/app/search';
		});
	};

	/**
	 * registers a user and logs in as a user, returns the result of $http.post(..)
	 */
	this.register = function(identifier, password){
		return $http.post('/api/session/register', {
	    	email: identifier,
	    	password: password
	    }).success(function(response) {
	    	config.identifier = identifier;
	    	config.password = password;
		});
	};

	/**
	 * registers a user and logs in as a user, returns the result of $http.post(..)
	 */
	this.logout = function(){
		config.identifier = null;
		config.password = null;
		window.location = '/';
	};

	this.isLoggedin = function(){
		return config.identifier != null;
	};

	this.isAdmin = function(){
		// TODO: In the future this can be mapped to the role of the user within its organisation(s)
		return config.identifier != null;
	};

	return this;

}]);

/**
 * [AngularJS debounce implementation](http://stackoverflow.com/questions/13320015/how-to-write-a-debounce-service-in-angularjs/13320016#13320016)
 */
angular.module('ddld.api').factory('debounce', ['$timeout','$q', function($timeout, $q) {
	// The service is actually this function, which we call with the func
	// that should be debounced and how long to wait in between calls
	return function debounce(func, wait, immediate) {
		var timeout;
		// Create a deferred object that will be resolved when we need to
		// actually call the func
		var deferred = $q.defer();
		return function() {
			var context = this, args = arguments;
			var later = function() {
				timeout = null;
				if(!immediate) {
					deferred.resolve(func.apply(context, args));
					deferred = $q.defer();
				}
			};
			var callNow = immediate && !timeout;
			if ( timeout ) {
				$timeout.cancel(timeout);
			}
			timeout = $timeout(later, wait);
			if (callNow) {
				deferred.resolve(func.apply(context,args));
				deferred = $q.defer();
			}
			return deferred.promise;
		};
	};
}]);
