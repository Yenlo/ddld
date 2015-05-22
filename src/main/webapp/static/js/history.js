(function(){

	angular.module('ddld.history', ['ddld.api']);

	angular.module('ddld.history').filter('reverse', function() {
	    return function(items) {
	        return items.slice().reverse();
	    };
	});

	angular.module('ddld.history').filter('setReverseIndex', function() {
	    return function(items, $scope) {
			var newitems = [];
			var count = 0;
			items.forEach(function(item){
				if($scope.relativePosition(item) > 0.01){
					newitems.push(item);
					item.reverseIndex = count++;
				}
				else{
					item.reverseIndex = -1;
				}
			});
			return newitems;
	    };
	});

	angular.module('ddld.history').controller('HistoryDisplayController', function($scope, $timeout, $http){

		$http.get('/api/plugin/pageview').success(function(pageviews){


			// map all the pageviews to find the lowest timestamp
			var lowest = Infinity;
			pageviews.forEach(function(pageview){
				lowest = Math.min(lowest, pageview.targetTimestamp);
			});

			// construct new object with the mock 
			var items = [{
				highlight: false,
				previous: null,
				reverseIndex: 0,
				time: lowest - 30000,
				url: "//digitaleleugendetector.nl/"
			}];

			// set up the rest of the items list
			pageviews.forEach(function(pageview){
				items.push({
					highlight: false,
					previous: null,
					previous_time: pageview.sourceTimestamp,
					reverseIndex: items.length,
					time: pageview.targetTimestamp,
					url: pageview.targetUrl
				});
			});

			// map all the pageviews to enable backreference search
			var mapping = [];
			items.forEach(function(item){
				var key = item.time + ";" + item.url;
				mapping[key] = item;
			});
			// connect them all by setting `previous`
			items.forEach(function(item){
				var prevkey = item.previous_time + ";" + item.url;
				item.previous = mapping[prevkey];
			});

			// $scope.items = items;
			console.log($scope.items, mapping);

		});

        // TODO: Currently this generates a basic example of what the history view would look like, this could then be connected to the actual logs
		var generateSampleItems = function(){
			var initial = [
				"http://digitaleleugendetector.nl",
				"http://stackoverflow.com",
				"http://serverfault.com/",
				"http://superuser.com/",
				"http://meta.stackoverflow.com/",
				"http://gamedev.stackexchange.com/",
				"http://security.stackexchange.com/",
				"http://security.stackexchange.com/questions",
				"http://security.stackexchange.com/questions/22903/why-refresh-csrf-token-per-form-request",
				"http://security.stackexchange.com/questions/6095/xkcd-936-short-complex-password-or-long-dictionary-passphrase",
				"http://security.stackexchange.com/questions/25684/how-can-i-explain-sql-injection-without-technical-jargon",
				"http://security.stackexchange.com/questions/20803/how-does-ssl-work",
				"http://security.stackexchange.com/questions/33470/what-technical-reasons-are-there-to-have-low-maximum-password-lengths",
				"http://security.stackexchange.com/questions/46569/is-it-bad-practice-to-use-your-real-name-online"
			];

			var samples = [].concat(initial).concat(initial).concat(initial).concat(initial).concat(initial).concat(initial);
			var items = [];
			for(var i in samples){
				var sample = samples[i];
				var time = Date.now() + ( Math.random() * 10000 ) + ( i * 11000 );
				var previous = i > 0 ? items[Math.floor(i * Math.random())] : undefined;
				items.push({
					reverseIndex: items.length,
					time: time,
					url: sample,
					previous: previous,
					highlight: (Math.random() > 0.8)
				});
			}
			return items;
		};

		$scope.items = generateSampleItems();

		/**
		 * returns the first item's time - 30000, or -30000 if there are no items.
		 */
		$scope.getMin = function(recalculate){
			if($scope.min == null || recalculate){
				$scope.min = $scope.items.length > 0 ? $scope.items[0].time - 30000 : -30000;
			}
			return $scope.min;
		};

		/**
		 * returns the last item's time + 30000, or 30000 if there are no items.
		 */
		$scope.getMax = function(recalculate){
			if($scope.max == null || recalculate){
				$scope.max = $scope.items.length > 0 ? $scope.items[$scope.items.length - 1].time + 30000 : 30000;
			}
			return $scope.max;
		};

		/**
		 * returns the relative position of this item from 0 to 1.
		 *
		 * for example 0.1 with:
		 *        minimum = 30
	         *        maximum = 90
		 *        item#time = 36
		 */
		$scope.relativePosition = function(item){
			var min = $scope.getMin();
			var max = $scope.getMax();
			var offsetMax = max - min;
			var offset = item.time - min;
			return ( offset / offsetMax );
		};

		/**
		 * returns the quadratic brezier curve SVG path from the item to item#previous relative to limit.
		 * @param item the item for which to calculate the path to the previous item
		 * @limit the amount to multiply the relative position with
		 * @indexMultiplier the amount to multiply the item#reverseIndex with
		 */
		$scope.qbcPath = function(item, limit, indexMultiplier){
			var itemX = Math.floor( $scope.relativePosition(item) * limit );
			var itemY = Math.floor(item.reverseIndex * indexMultiplier );
			if(item.previous && item.previous.reverseIndex != -1){
				itemX = Math.floor($scope.relativePosition(item) * limit );
				itemY = Math.floor(item.reverseIndex * indexMultiplier );
				var prevX = Math.floor($scope.relativePosition(item.previous) * limit );
				var prevY = Math.floor(item.previous.reverseIndex * indexMultiplier );
				return 'M' + prevX + ',' + prevY + ' Q' + prevX + ',' + itemY + ' ' + itemX + ',' + itemY;
			}
			else{
				return 'M' + 0 + ',' + itemY + ' Q' + itemX + ',' + itemY + ' ' + itemX + ',' + itemY;
			}
		};

		$scope.scale = 1;

		$scope.click = function(item){
			item.highlight = !item.highlight;
		};

		$scope.setShowpath = function(item){
			var current = item;
			for(var i in $scope.items){
				$scope.items[i].showpath = false;
			}
			do {
				current.showpath = true;
				current = current.previous;
			} while(current != null);
		};

	});

})();
