(function(appAPI){

	var notify = function(message){
		appAPI.notifier.show({
		    'name': Math.random() + '',
		    'title': 'Digitale Leugendetector',
		    'body': message,
		    'link': 'https://digitaleleugendetector.nl/',
		    'theme': 'default',
		    'position': 'top-right',
		    'close': true,
		    'sticky': false,
		    'fadeAfter': 10,
		    'width': '400px',
		    'closeWhenClicked': true
		});
	};

	if(location.hostname === 'digitaleleugendetector.nl'){

		window.appAPI = appAPI;

		/**
		 * DDLD-only processes
		 */

		setInterval(function(){
			var element = document.getElementById('ddld-plugin-installed');
			if(element){
				element.innerHTML = ' <i class="fa fa-fw">&#xf00c</i>';
			}
		}, 500);

		setInterval(function(){
			var conf = localStorage.SessionServiceConfig;
			if(conf != null){
				var confObject = JSON.parse(conf);
				var identifier = confObject.identifier;
				var password = confObject.password;
				if(identifier != null && password != null){
					var key = 'Basic ' + window.btoa(identifier + ':' + password);
					if(key != appAPI.db.get('key')){
						appAPI.db.set('key', key);
						notify('De DDLD plugin is zojuist gekoppeld aan je account.');
					}
				}
			}
		}, 2000);

	}

	var config = null;
	var randid = Math.random();
	var href = location.href;

	/**
	 * Listen for any messages the background script may be sending.
	 */
    appAPI.message.addListener(function(msg) {

    	if(msg.scope == 'background:hello'){
			if(msg.location == href && msg.randid == randid){
				console.log(msg.scope, 'incoming message is meant for this instance, store config and begin indexing');
				config = msg.config;
				console.log(msg.scope, 'configuration', config);

				if(appAPI.sidebar){

					var sidebar = new appAPI.sidebar({
						position: 'right',
						url: 'https://digitaleleugendetector.nl/app/sidebar',
						opacity: 1.0,
						width: '300px',
						height: '650px',
						preloader: false,
						sticky: true,
						slide: 150,
						openAction: ['click'],
						closeAction: 'click',
						theme: 'default',
						scrollbars: false,
						openOnInstall: true,
						events: {
							onShow:function () {},
							onHide:function () {}
						}
					});

					console.log(sidebar);

				}

				var key = appAPI.db.get('key');
				if(key != null){
					setTimeout(function(){
						console.log('indexing', href);
						appAPI.request.post({
							url: 'https://digitaleleugendetector.nl/api/document/process',
							additionalRequestHeaders: {
								"Authorization": key
							},
							postData: {
								key: key,
								url: href,
								title: document.title,
								content: document.documentElement.innerHTML.replace(/\s+/g, ' ')
							},
							contentType: 'application/json',
							onSuccess: function(response, additionalInfo) {
								appAPI.message.toBackground({
							        scope: 'plugin:indexed',
							        location: href,
							        indexid: JSON.parse(response).id
							    });
								console.log('indexing success', href, response);
							},
							onFailure: function(httpCode) {
								console.log('indexing failed, ', arguments);
								notify('Het indexeren is mislukt, controleer of je verbinding kunt maken met https://digitaleleugendetector.nl/');
							}
						});
					}, 5000);
				}
				else {
					notify('De plugin is momenteel niet aan een account gekoppeld. Log in op https://digitaleleugendetector.nl/ en de plugin koppelt zich automatisch aan je account.');
				}

			}
    	}

    });

	/**
	 * Send a message to the background page
	 */
	appAPI.message.toBackground({
		scope: 'plugin:hello',
		location: href,
		randid: randid
	});

});