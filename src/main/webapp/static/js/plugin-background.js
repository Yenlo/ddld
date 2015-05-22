(function(appAPI){

	/**
	 * Listen for any messages the page script may be sending.
	 */
    appAPI.message.addListener(function(msg) {

    	/**
    	 * This is a message the plugin will send when it has indexed the page's contents.
    	 * 
    	 * It should contain an `indexid` field which references the indexed document's index id.
    	 */
    	if(msg.scope == 'plugin:indexed'){
    		var view = views[msg.location];
    		if(view){
    			console.log(msg.scope, 'known view, pushing indexid', msg);
    			view.ids.push(msg.indexid);
    		}
    		else{
    			console.log(msg.scope, 'no known view', msg);
    		}
    	}

    	/**
    	 * This is a message the plugin will always send to initialize with the background page,
    	 * pages should only be indexed if they are known within the background page tracked pages.
    	 * 
    	 * The backround page script responds by broadcasting a 'background:hello' message, which the
    	 * page script must verify itself if the message's location and randid properties equal what it sent;
    	 * If these match the plugin page knows that the message was meant for this plugin.
    	 */
    	if(msg.scope == 'plugin:hello'){
    		var view = views[msg.location];
    		if(view){
    			console.log(msg.scope, 'known view, responding with "background:hello"', msg);
        		appAPI.message.toAllTabs({
        			scope: 'background:hello',
        			location: msg.location,
        			randid: msg.randid,
        			// TODO: this is now an empty config object, this is to be configurable via the sidebar
        			config: {}
        		});
    		}
    		else{
    			console.log(msg.scope, 'no known view', msg);
    		}
    	}

    });

	var DDLD = '//digitaleleugendetector.nl/';

	var tracked = [];

	/**
	 * This is constructed to become the tree of tracked pages
	 * Most of the time this variable will have a cyclic structure
	 */
	var views = {};
	views[DDLD] = {
		name: DDLD,
		times: [
	        Date.now()
        ],
		ids: [],
		children: [
			// references to inside views
		]
	};

	/**
	 * Allows for cyclic structures to be stringified
	 * @author http://stackoverflow.com/a/14555614/1066946
	 */
	var stringifyOnce = function(obj, replacer, indent){
	    var printedObjects = [];
	    var printedObjectKeys = [];

	    function printOnceReplacer(key, value){
	        var printedObjIndex = false;
	        printedObjects.forEach(function(obj, index){
	            if(obj===value){
	                printedObjIndex = index;
	            }
	        });

	        if(printedObjIndex && typeof(value)=='object'){
	            return '(see ' + value.constructor.name.toLowerCase() + ' with key ' + printedObjectKeys[printedObjIndex] + ')';
	        } else {
	            var qualifiedKey = key || '(empty key)';
	            printedObjects.push(value);
	            printedObjectKeys.push(qualifiedKey);
	            if(replacer){
	                return replacer(key, value);
	            }else{
	                return value;
	            }
	        }
	    }
	    return JSON.stringify(obj, printOnceReplacer, indent);
	};

	var track = function(url, previous){
		tracked.push({
			time: Date.now(),
			url: url,
			previous: previous
		});
		var view = views[url];
		if(view == null){
			view = {
				name: url,
				times: [],
				children: [],
				ids: [],
				parent: previous
			};
			views[url] = view;
		}
		var previousView = views[previous];
		if(previousView){
			previousView.children.push(view);
		}
		view.times.push(Date.now());
        // for debugging via plugin background page
		window.tree = JSON.parse(stringifyOnce(views[DDLD]));
		window.views = views;
	};

	/**
	 * Tab abstraction, all events are converted to an slightly more object oriented structure making tracking much easier.
	 */
	var Tab = function(id, url, parent, notrack){

		this.parent = parent;
		this.id = id;

		if(url && url.indexOf(DDLD) != -1){
			this.notrack = false;
			this.url = DDLD;
		}
		else if(this.isUseableUrl(url)){
			this.notrack = parent ? parent.notrack : ( notrack || false );
			this.url = url;
		}
		else{
			this.notrack = true;
			this.url = null;
		}
		this.view(this.url, this.parent ? this.parent.url : null);

	};

	Tab.prototype.view = function(url, previous){
		if(this.notrack == false && url != null){
			track(url, previous);
			var key = appAPI.db.get('key');
			if(key != null){
				appAPI.request.post({
					url: 'https://digitaleleugendetector.nl/api/plugin/pageview',
					additionalRequestHeaders: {
						"Authorization": key
					},
					postData: {
						targetTimestamp: Date.now(),
						targetUrl: url,
						sourceTimestamp: -1,
						sourceUrl: previous
					},
					contentType: 'application/json',
					onSuccess: function(response, additionalInfo) {
						console.log('track success', href, response);
					},
					onFailure: function(httpCode) {
						console.error('track failed, ', arguments);
					}
				});
			}
		}
	};

	/**
	 * Returns true when the tab is of interest for the user to track
	 * for example all chrome:* urls are not useable; this could be a blank page, extension or debug window.
	 */
	Tab.prototype.isUseableUrl = function(url){
		return url && (url.indexOf('http://') == 0
					|| url.indexOf('https://') == 0
					|| url.indexOf('ftp://') == 0);
	};

	/**
	 * sets the url of the tab ( if useable, otherwise nulls out )
	 * returns the previous value of this.url
	 */
	Tab.prototype.updateUrl = function(url){
		var previous = this.url;
		var actual = this.isUseableUrl(url) ? url : null;
		if(actual && actual.indexOf(DDLD) != -1){
			actual = DDLD;
		}
		if(this.url != actual){
			if(actual == DDLD){
				this.notrack = false;
			}
			this.view(actual, this.url);
			this.url = actual;
		}
		return previous;
	};

	/**
	 * Browsers do not behave the same, and it happens quite often that an event is missed by CrossRider's framework.
	 * This gets all tabs, then initializes event listeners which manage the mapping of tabs and the active tab.
	 */
	appAPI.tabs.getAllTabs(function(initialTabs) {

		// The current active tab
		var active = null;
		// Mapping of all open tabs
		var tabs = {};

		initialTabs.forEach(function(tab){
			var id = tab.tabId;
			var url = tab.tabUrl;
			// We add all the initial tabs as tabs the user does not want to track. This way all tabs and their
			// descendants are never tracked, only those initialized from the DDLD website or via the plugin, are.
			tabs[id] = new Tab(id, url, null, true);
		});

    	/**
    	 * Called periodically and when the selection of tab changes, this pushes active tabs.
    	 */
    	var updateActive = function(tabInfo){
        	var id = tabInfo.tabId;
        	if(!tabs.hasOwnProperty(id)){
        		console.warn('Tab active event, while not created! Assuming child of `active`', active ? active.url : 'none active');
            	var url = tabInfo.tabUrl;
        		tabs[id] = new Tab(id, url, active);
        	}
        	var tab = tabs[id];
        	if(tab != active){
        		active = tab;
        	}
        	if(tabInfo.tabUrl != active.url){
            	active.updateUrl(tabInfo.tabUrl);
        	}
    	};

        appAPI.tabs.onTabSelectionChanged(updateActive);
        setInterval(function(){
        	appAPI.tabs.getActive(updateActive);
        }, 500);

        /**
         * Updates the URL for whenever a Tab is updated ( _loaded_ ).
         * This occurs only when a page is switched; user clicks a link and the window is unloaded and loaded.
         * ( Also creates the tab if it does not exist, assumes cross browser the behaviour may be different )
         */
        appAPI.tabs.onTabUpdated(function(tabInfo) {
        	var id = tabInfo.tabId;
        	var url = tabInfo.tabUrl;
        	if(!tabs.hasOwnProperty(id)){
        		console.warn('Tab update event, while not created! Assuming child of `active`', active ? active.url : 'none active');
        		tabs[id] = new Tab(id, url, active);
        	}
            var tab = tabs[id];
            tab.updateUrl(url);
        });

        /**
         * Creates a Tab object whenever a tab is created.
         */
        appAPI.tabs.onTabCreated(function(tabInfo) {
        	// we have to go via getAllTabs because on onTabCreated the url is often null, even when the tan contains the url
        	appAPI.tabs.getAllTabs(function(allTabs) {
        		allTabs.forEach(function(iTab){
        			if(iTab.tabId == id){
        	        	var id = tabInfo.tabId;
        	        	var url = tabInfo.tabUrl;
        	        	if(!tabs.hasOwnProperty(id)){
        	        		tabs[id] = new Tab(id, url, active);
        	        	}
        	        	else{
        	        		console.warn('Tab create event, while already created!');
        	        	}
        			}
        		});
            });

        });

        /**
         * Notifies a Tab that it is closed if there is any, and then purges it from the tabs map.
         * If the active tab is the tab being closed active is also nulled out.
         */
        appAPI.tabs.onTabClosed(function(tabInfo) {
        	var id = tabInfo.tabId;
        	if(tabs.hasOwnProperty(id)){
            	var tab = tabs[id];
            	delete tabs[id];
            	if(active == tab){
            		active = null;
            	}
        	}
        	else{
        		console.warn('Tab close event, while not created!');
        	}
        });

	});

});