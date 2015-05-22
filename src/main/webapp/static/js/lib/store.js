/**
 * A storage service that persists to localStorage, maintains an internal cache as well, to avoid large JSON parsing on .get calls.
 * Provides utilities to watch for changes, synchronizes cross-window and can even two-way-bind objects in Store with objects on a $scope.
 * 
 * @ https://github.com/SkPhilipp/Store
 * 
 */
angular.module("SkP", []).provider("Store", function () {

	var instance = (function($window, $parse){
		
		var that = this;
		var cache = {};
		var watched = {};
	
		this.get = function(key) {
			if(cache.hasOwnProperty(key)){
				return cache[key];
			}
			else{
				var result = localStorage.getItem(key);
				return result == null ? result : angular.fromJson(result);
			}
		};
	
		/**
		 * Checks if we have the object, less expensive than actually going there and maybe even having to parse it.
		 */
		this.has = function(key){
			return cache.hasOwnProperty(key) || localStorage.hasOwnProperty(key);
		};
	
		this.set = function(key, value) {
			if(value != undefined){
				cache[key] = value;
				localStorage.setItem(key, angular.toJson(value));
			}
		};
	
		/**
		 * Watches an item for changes, when they happen the given callback will be called.
		 * Note that the callback will not be $scope.$apply'd.
		 */
		this.watch = function(key, callback){
			watched[key] = ([]||watched[key]);
			watched[key].push(callback);
		};
	
		angular.element($window).bind("storage", function(angularEvent){
			var event = angularEvent.originalEvent;
			cache[event.key] = event.newValue;
			if(watched.hasOwnProperty(event.key)){
				var value = angular.fromJson(event.newValue);
				for(i in watched[event.key]){
					watched[event.key][i](value);
				}
			}
		});
	
		/**
		 * Provides a two-way-binding between localStorage and a $scope object.
		 * Uses $watch to compare for equality, not reference.
		 * 
		 * Do not use primitive types as values for 'defaults'.
		 * 
		 * Can be invoked with the following types:
		 * (object, string, string, object)
		 * (object, string, string) < preferred
		 * (object, string, object)
		 * (object, string)
		 */
		this.mirror = function($scope, scopeKey, storeKey, defaults){

			// when storeKey parameter is ommitted
			if(!(typeof storeKey == "string")){
				storeKey = scopeKey;
			}

			// using second type of overload
			if(defaults == undefined && typeof storeKey == "object"){
				defaults = storeKey;
			}

			if(typeof defaults != "object"){
				if(defaults != null){
					console.warn("Mirroring primitives or non-object types is not supported.");
				}
				defaults = {};
			}

			var getter = $parse(scopeKey);
			var setter = getter.assign;
	
			// we the reference to this callback on delete
			var callback = function(value){
				setter($scope, value);
				$scope.$apply();
			};
	
			// add it to watched so changes in localStorage go to $scope
			watched[storeKey] = watched[storeKey] || [];
			watched[storeKey].push(callback);
	
			// when the scope is destroyed, clean up watched
			$scope.$on("$destroy", function() {
				var index = watched[storeKey].indexOf(callback);
				watched[storeKey].splice(index, 1);
				if(watched[storeKey].length == 0){
					delete watched[storeKey];
				}
			});
	
			// watch for changes from $scope
			$scope.$watch(scopeKey, function(newValue, oldValue){
				that.set(storeKey, newValue);
			}, true);
	
			// initialize $scope when $scope has no value and Store does
			if(getter($scope) == null && that.has(storeKey)){
				setter($scope, that.get(storeKey));
			}
	
			// initialize Store when $store has no value and $scope does
			if(!that.has(storeKey) && getter($scope) != null){
				that.set(storeKey, getter($scope));
			}

			// initialize with defaults if neither has a value
			if(!that.has(storeKey) && getter($scope) == null){
				that.set(storeKey, defaults);
				setter($scope, defaults);
			}

			// return scope reference
			return $scope[scopeKey];
	
		};
	
		return this;

	});
	
	this.$get = ['$window', '$parse', function($window, $parse){
		return new instance($window, $parse);
	}];

});