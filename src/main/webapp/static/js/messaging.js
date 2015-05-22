// send to sidebar page from plugin
// send from sidebar page to plugin
// send to plugin from ddld page
// send from ddld page to plugin
// send from background to main window
// send to background from main window

// on page index
	// send from plugin.js to plugin-background.js that page was indexed
	// plugin-background.js needs listener for page-index event message
	// plugin.js needs send to background page-index event message

// on config change in sidebar
	// detect change via sidebar-only plugin.s subset
	// send from sidebar-only plugin.js subset that config was changed to plugin.js

// on load in sidebar
	// send from sidebar-only plugin.js subset request to background-plugin.js to obtain the history

// on tree page
	// open the sidebar

if (appAPI.dom.isIframe() && $('#mySidebar').length) {
    // Set click handler for button to send message to parent window
    $('#btnSave').click(function() {
      appAPI.message.toCurrentTabWindow({
        type:'save',
        data:'My save data'
      });
    });
    // End of Iframe code ... exit
    return;
}

appAPI.ready(function($) {
    // Sends a message to the background code instructing all tabs to remove a shortcut
    //
    // The background code's listener event handler determines the scope from the message
    // and, in this example, relays the message to all tabs using appAPI.message.toAllTabs
    appAPI.message.toBackground({
        scope: 'all-tabs',
        action:'remove-shortcut',
        shortcut:'Ctrl+X'
    });
});

appAPI.ready(function($) {
    // Sends a message to all tabs to change the background color to red
    appAPI.message.toAllTabs({action:'change-color', color:'red'});
});

appAPI.ready(function($) {
    // Adds a listener that broadcasts incoming messages to all tabs
    var id = appAPI.message.addListener(function(msg) {
        appAPI.message.toAllTabs(msg);
		if (msg.type === 'save') {
		  console.log('Extn:: Parent received data: ' +
			appAPI.JSON.stringify(msg.data));
		}
    });

    // Adds a listener that receives a message on the "page" channel to change the background color
    // Message: {action:'change-color', color:'red'}
    appAPI.message.addListener({channel: "page"}, function(msg) {
        if (msg.action == 'change-color') {
            $('body').css('background-color', msg.color);
        }
    });
});