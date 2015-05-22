/**
 * 'ddld' module definition, we make use of the following:
 * - ddld.api the ddld api clients.
 * - ddld.tree for the tree view.
 * - notifications for alerts on functional or technical ajax errors.
 * - SkP from store js, for Store, for persisting short lived options in localStorage.
 * - ngRoute from angular-route js, for routing with ng-view.
 * - ngAnimate from angular-animate js, for animating fade ins and outs.
 * - ui.bootstrap Angular UI Bootstrap directives.
 */
angular.module('ddld', ['ddld.api', 'ddld.history', 'notifications', 'SkP', 'ngRoute', 'ngAnimate', 'truncate', 'ui.bootstrap']);

/**
 * Routing configuration, any new pages must also be registered here, if it has any functionality, add a controller.
 * - any single-page functionality should be put into the controller logic for that page.
 * - any shared functionality should be put into a service or service equivalent.
 */
angular.module('ddld').config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider){

    $routeProvider.when('/', {
        templateUrl: '/views/index.html'
    });

    $routeProvider.when('/app/sources', {
        templateUrl: '/views/sources.html',
        controller: 'SourcesController'
    });

    $routeProvider.when('/app/sidebar', {
        templateUrl: '/views/sidebar.html',
        controller: 'SidebarController'
    });

    $routeProvider.when('/app/push', {
        templateUrl: '/views/push.html',
        controller: 'PushController'
    });

    $routeProvider.when('/app/profile', {
        templateUrl: '/views/profile.html',
        controller: 'ProfileController'
    });

    $routeProvider.when('/app/login', {
        templateUrl: '/views/login.html',
        controller: 'LoginController'
    });

    $routeProvider.when('/app/register', {
        templateUrl: '/views/register.html',
        controller: 'RegisterController'
    });

    $routeProvider.when('/app/users', {
        templateUrl: '/views/users.html',
        controller: 'UsersController'
    });

    $routeProvider.when('/app/search', {
        templateUrl: '/views/search.html',
        controller: 'SearchController'
    });

    $routeProvider.when('/app/tree', {
        templateUrl: '/views/history.html',
        controller: 'HistoryDisplayController'
    });

    $routeProvider.when('/app/article', {
        templateUrl: '/views/article.html',
        controller: 'ArticleController'
    });

    $routeProvider.when('/app/404', {
        templateUrl: '/views/errors/404.html'
    });

    $routeProvider.otherwise({redirectTo: '/app/404' });

    $locationProvider.html5Mode(true);

}]);

/**
 * controller for views/index.html
 */
angular.module('ddld').controller('MainController', ['$scope', 'SessionService', '$location', '$rootScope', function($scope, SessionService, $location, $rootScope){

    $scope.isLoggedin = SessionService.isLoggedin;
    $scope.getUsername = SessionService.getIdentifier;
    $scope.isAdmin = SessionService.isAdmin;

    /**
     * clears the session and returns to the frontpage
     */
    $scope.logout = function(){
        SessionService.logout().then(function(){
            $location.path('/');
        });
    };

    /**
     * returns true if the current location path is the given where path
     */
    $scope.is = function (where) { 
        return where == $location.path();
    };

}]);

/**
 * controller for views/article.html > navigation
 */
angular.module('ddld').controller('ArticleController', ['$scope', '$location', '$http', '$routeParams', function($scope, $location, $http, $routeParams){

	var options = $location.search();

	$scope.loading = true;
	$http.get('/api/document/article?id=' + escape(options.id) + '&engine=' + escape(options.engine))
		.success(function(article){
	    	$scope.loading = false;
			$scope.article = article;
		})
		.error(function(){
			$scope.loading = false;
		});

}]);

/**
 * controller for views/login.html
 */
angular.module('ddld').controller('LoginController', ['$scope', '$location', 'SessionService', '$rootScope', function($scope, $location, SessionService, $rootScope){

    $scope.login = function(){
    	$scope.loading = true;
        SessionService.login($scope.formEmail, $scope.formPassword).success(function(){
        	$scope.loading = false;
            $location.path('/app/search');
        }).error(function(){
        	$scope.loading = false;
        });
    };

}]);

/**
 * controller for views/login.html
 */
angular.module('ddld').controller('RegisterController', ['$scope', '$location', 'SessionService', '$rootScope', function($scope, $location, SessionService, $rootScope){

    $scope.register = function(){
    	$scope.loading = true;
        SessionService.register($scope.formEmail, $scope.formPassword).success(function(){
        	$scope.loading = false;
            $location.path('/app/search');
        }).error(function(){
        	$scope.loading = false;
        });
    };

}]);

/**
 * controller for views/users.html
 */
angular.module('ddld').controller('UsersController', ['$scope', 'Store', function($scope, Store){

    /**
     * all users within the organisation
     */
    $scope.users = [
        { id: 1233, name: "J.A. List", role: "Admin", email: "jalist@example.com" },
        { id: 1234,  name: "J.A. Liste", role: "Gebruiker", email: "jaliste@example.com" }
    ];

    /**
     * everything concerning the `create user` modal and methods on it
     */
    $scope.modal = {
        username: "",
        email: "",
        password: "",
        password2: "",
        profiles: [],
        /**
         * adds or removes an organisation profile to the modal profile
         */
        toggleProfile: function(profile){
            
        },
        /**
         * prompt for a confirmation screen
         */
        confirmCreateProfile: function(){
            confirm("Zeker weten? Hiermee sluit namelijk het nieuwe gebruiker scherm.");
        },
        /**
         * creates a user and resets the modal inputs
         */
        createUser: function(){
            $scope.modal.username = "";
            $scope.modal.email = "";
            $scope.modal.password = "";
            $scope.modal.password2 = "";
        }
    };

    /**
     * these are the profiles available within the organisation
     */
    $scope.profiles = [
        { name: "Wetenschap" },
        { name: "Politiek" },
        { name: "Default profiel" }
    ];

}]);

/**
 * controller for views/profile.html
 */
angular.module('ddld').controller('ProfileController', ['$scope', 'Store', function($scope, Store){

}]);

/**
 * controller for views/sources.html
 */
angular.module('ddld').controller('SourcesController', ['$scope', 'Store', function($scope, Store){

}]);

/**
 * controller for views/sidebar.html
 */
angular.module('ddld').controller('SidebarController', ['$scope', 'Store', function($scope, Store){

}]);


/**
 * controller for the left `push` sidebar
 */
angular.module('ddld').controller('PushSidebarController', ['$scope', 'Store', '$http', '$timeout', 'SessionService', function($scope, Store, $http, $timeout, SessionService){

	$scope.crawlers = [];

    $scope.selected = null;
    $scope.highlight = false;
    $scope.maxSize = 7;
	$scope.crawlHits = {};
	$scope.crawlHighlights = {};

    $scope.$watch('currentPage', function(page){
        $scope.loadPages(page);
    });

    $scope.loadPages = function(page){
    	var offset = ( (page - 1) || 0 ) * 10;
    	if($scope.selected != null){
	    	$scope.paginationState = 'loading';
			var url = '/api/crawler/' + $scope.selected.id + ( $scope.highlight ? '/highlights' : '/documents');
				url = url + '?since=0';
				url = url + '&max=10';
				url = url + '&offset=' + offset;
			$http.get(url).success(function(data){
				$scope.paginationState = 'loaded';
				$scope.selectedResults = data.items;
			});
    	}
    };
   
	$scope.openCrawlerModal = function(crawler, highlight){
	    $scope.selected = crawler;
		$scope.currentPage = 1;
	    $scope.highlight = highlight;
		$scope.currentItems = ( $scope.highlight ? $scope.crawlHighlights[$scope.selected.id] : $scope.crawlHits[$scope.selected.id] );
        $scope.loadPages();
        $('#pushModalController').modal('show');
	};

	/**
	 * (Re)Loads the crawlers to $scope.crawlers
	 */
	$scope.loadState = 'none';
	$scope.loadCrawlers = function(){
		if(SessionService.isLoggedin()){
			$scope.loadState = 'loading';
			$http.get('/api/crawler')
				.success(function(data, status, headers, config){
					$scope.crawlers = data;
					$scope.loadState = 'loaded';
					$scope.crawlers.forEach(function(crawler){
						var url = '/api/crawler/' + crawler.id + '/documents';
							url = url + '?since=0';
							url = url + '&max=0';
							url = url + '&offset=0';
						$http.get(url).success(function(data){
							$scope.crawlHits[crawler.id] = data.estimatedHits;
						});
					});
					$scope.crawlers.forEach(function(crawler){
						var url = '/api/crawler/' + crawler.id + '/highlights';
							url = url + '?since=0';
							url = url + '&max=0';
							url = url + '&offset=0';
						$http.get(url).success(function(data){
							$scope.crawlHighlights[crawler.id] = data.estimatedHits;
						});
					});
				});
		}
	};

	var promise = null;
	var refresh = function(){
		$scope.loadCrawlers();
		if(promise){
			$timeout.cancel(promise);
		}
		promise = $timeout(refresh, 10000);
	};

	$scope.$on('$destroy', function(event) {
            $timeout.cancel(promise);
    });

	refresh();

}]);

/**
 * controller for views/push.html
 */
angular.module('ddld').controller('PushController', ['$scope', 'Store', '$http', '$timeout', function($scope, Store, $http, $timeout){

	$scope.crawlers = [];

	$scope.crawlTypes = {
		'ALL': {
			description: 'Alle webpaginas'
		}
	};

	$scope.frequency = 123;

	$scope.asNumber = function(string, limit){
		return Math.min(parseInt(string) || 1, limit || 60);
	};

	$scope.frequencyTypes = {
		'MINUTELY': {
			description: function(n){
				return n == 1 ? 'Elke minuut' : ( 'Om de ' + n + ' minuten' );
			},
			hasInterval: true
		},
		'HOURLY': {
			description: function(n){
				return n == 1 ? 'Elk uur' : ( 'Om de ' + n + ' uur' );
			},
			hasInterval: true
		},
		'DAILY': {
			description: function(n){
				return n == 1 ? 'Elke dag' : ( 'Om de ' + n + ' dagen' );
			},
			hasInterval: true
		},
		'WEEKLY': {
			description: function(n){
				return 'Wekelijks';
			},
			hasInterval: false
		},
		'MONTHLY': {
			description: function(n){
				return 'Maandelijks';
			},
			hasInterval: false
		}
	};

	/**
	 * (Re)Loads the crawlers to $scope.crawlers
	 */
	$scope.loadState = 'none';
	$scope.loadCrawlers = function(){
		$scope.loadState = 'loading';
		$http.get('/api/crawler')
			.success(function(data, status, headers, config){
				$scope.crawlers = data;
				$scope.loadState = 'loaded';
			});
	};

	var promise = null;
	var refresh = function(){
		$scope.loadCrawlers();
		if(promise){
			$timeout.cancel(promise);
		}
		promise = $timeout(refresh, 10000);
	};

	$scope.$on('$destroy', function(event) {
            $timeout.cancel(promise);
    });

	refresh();

	/**
	 * Creates a crawler
	 */
    $scope.createState = 'none';
	$scope.createCrawler = function(title, root, depth, external, type, frequencyType, frequency, filter){
		var submittable = {
			title: title,
			root: root,
			depth: depth || 2,
			external: external,
			type: type,
			frequencyType: frequencyType,
			frequency: frequency,
			filter: filter
		};
		$scope.createState = 'loading';
        $http.post('/api/crawler/', submittable).
          success(function(data, status, headers, config) {
      		$scope.createState = 'none';
      		refresh();
        });
	};

	/**
	 * Updates a crawler
	 */
    $scope.updateState = 'none';
	$scope.updateCrawler = function(id, title, root, depth, external, type, frequencyType, frequency, filter){
		var submittable = {
			title: title,
			root: root,
			depth: depth,
			external: external,
			type: type,
			frequencyType: frequencyType,
			frequency: frequency,
			filter: filter
		};
		$scope.updateState = 'loading';
        $http.post('/api/crawler/' + id, submittable).
          success(function(data, status, headers, config) {
      		$scope.updateState = 'none';
      		refresh();
        });
	};

	/**
	 * Deletes a crawler
	 */
    $scope.deleteState = 'none';
	$scope.deleteCrawler = function(id){
		$scope.deleteState = 'loading';
        $http.get('/api/crawler/' + id + '/delete').
          success(function(data, status, headers, config) {
      		$scope.deleteState = 'none';
      		refresh();
        });
	};

}]);

/**
 * controller for views/search.html
 */
angular.module('ddld').controller('SearchController', ['debounce', '$scope', '$routeParams', 'Store', 'SessionService', '$http', function(debounce, $scope, $routeParams, Store, SessionService, $http){

    /**
     * everything concerning the `create user` modal and methods on it
     */
    $scope.modal = {
        state: 'ready',
        name: '',
        /**
         * creates a factcheck and activates it if it is the user's only factcheck.
         */
        create: function(){
            $scope.modal.state = 'loading';
            $http.post('/api/factcheck/', {name: $scope.modal.name}).
              success(function(data, status, headers, config) {
                  $scope.factchecks.refreshItems();
                  $scope.modal.state = 'ready';
                  $scope.modal.name = '';
                  $('#newFactcheckModal').modal('hide');
            });
        }
    };

    $scope.factchecks = {
        /**
         * Currently active factcheck id
         */
        active: null,
        items: [],
        refreshItems: function(){
        	$scope.factchecks.refreshItems.loading = true;
            $http.get('/api/factcheck/').
                success(function(data, status, headers, config) {
                	$scope.factchecks.refreshItems.loading = false;
                    $scope.factchecks.items = data;
                }).
                error(function(data, status, headers, config) {
                	$scope.factchecks.refreshItems.loading = false;
                });
            $http.get('/api/factcheck/active').
                success(function(data, status, headers, config) {
                    $scope.factchecks.active = data.length > 0 ? parseInt(data) : null;
                });
        },
        /**
         * Selecting a factcheck activates it
         */
        select: function(item){
        	if($scope.factchecks.active == null || confirm("Wanneer je nieuwe paginas indexeert zullen ze onder deze factcheck terecht komen, zeker weten?")){
        		$scope.factchecks.select.loading = true;
            	$http.get('/api/factcheck/' + item.id + '/use').
	              success(function(data, status, headers, config) {
	            	  $scope.factchecks.select.loading = false;
	           	      $scope.factchecks.active = item.id;
	                  $scope.search.query($scope.search.input);
	              }).
	              error(function(data, status, headers, config) {
	            	  $scope.factchecks.select.loading = false;
	              });
        	}
        },
        /**
         * Deletes a factcheck
         */
        remove: function(item){
        	if(confirm("Verwijderen kan niet ongedaan gemaakt worden, weet je het zeker? Geindexeerde bestanden en je boomstructuur verwijder je mee met je factcheck.")){
        		$scope.factchecks.remove.loading = true;
        	    $http.get('/api/factcheck/' + item.id + '/delete').
		            success(function(data, status, headers, config) {
		            	$scope.factchecks.remove.loading = false;
		                if(item.id == $scope.factchecks.active){
		                    $scope.factchecks.active = null;
		                }
		                var index = 0;
		                $scope.factchecks.items.forEach(function(current, key){
		                    if(current.id == item.id){
		                        index = key;
		                    }
		                });
		                $scope.factchecks.items.splice(index, 1);
		        }).
		        error(function(data, status, headers, config) {
		        	$scope.factchecks.remove.loading = false;
		        });
        	}
        }
    };

    $scope.factchecks.refreshItems();

    $scope.search = {
        /**
         * the search field query string
         */
        input: '',
        /**
         * any items that the backend thinks are related to the current query
         */
        related: {
            items: [
                { name: 'Obesitas' },
                { name: 'Cybercrime' }
            ]
        },
        /**
         * the main method where the query call happens to the backend, when this is called
         * - any results will be put into $scope.results
         * - any related items will be added to $scope.search.related
         */
        query: function(querystring, max, offset){
            if(querystring.length > 0){
                // clear the resultset
                $scope.results = {};
                // get all `enabled` engines
                var engines = [];
                $scope.sources.forEach(function(type){
                    type.items.forEach(function(source){
                        if(source.enabled){
                            engines.push(source.engine);
                        }
                    });
                });
                // query all engines, and push the result to the $scope.results map
                engines.forEach(function(engine){
                    $scope.results[engine] = {
                        state: "loading"
                    };
                    var max = max || 10;
                    var offset = offset || 0;
                    $http({method: 'GET', url: '/api/document/search?query=' + escape(querystring)
                                             + '&engine=' + escape(engine)
                                             + '&max=' + escape(max)
                                             + '&offset=' + escape(offset)
                                             }).
                        success(function(data, status, headers, config) {
                            $scope.results[engine].state = "done";
                            $scope.results[engine].items = data.items;
                            $scope.results[engine].estimatedHits = data.estimatedHits;
                        })
                        .error(function(data, status, headers, config) {
                            $scope.results[engine].state ="erred";
                          });
                });
            }
        },
        /**
         * Highlights a given document
         */
        highlight: function(item){
        	var highlighted = item.highlighted ? false : true;
        	$http.get('/api/document/highlight/' + item.id + '/' + highlighted)
	            .success(function(data, status, headers, config) {
	            	item.highlighted = highlighted;
	            });
	    },
        /**
         * Removes a given document
         */
        remove: function(item){
        	$http.get('/api/document/remove/' + item.id)
	            .success(function(data, status, headers, config) {
	            	item.removeDisabled = true;
	            });
	    }
    };
    
    $scope.search.queryDebounce = debounce($scope.search.query, 1000, false);

    /**
     * source configuration editable by user in search options, kept mirrored with Store
     */
    $scope.sources = [
        {
              type: 'Web',
              items: [
                {
                    name: 'Bing',
                    enabled: false,
                    engine: 'bing-web'
                },
                {
                    name: 'DLD',
                    enabled: true,
                    engine: 'dld'
                }
            ]
        },
        {
              type: 'Nieuws',
              items: [
              {
                  name: 'Newz',
                  enabled: true,
                  engine: 'newz'
              },
              {
                  name: 'Bing News',
                  enabled: false,
                  engine: 'bing-news'
              }
            ]
        }
    ];

    /**
     * additional search options, kept mirrored with Store
     */
    $scope.options = Store.mirror($scope, 'options') || {
        'only_from_nl': false,
        'only_in_nl': false
    };

}]);
