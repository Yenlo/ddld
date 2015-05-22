/*
 * boilerplate to set up ILPS Logging
 */

(function() {
    var script = document.createElement('script');
    script.async = true;
    script.src = 'http://ilpslogging.staging.dispectu.com/jssdk/ilpslogging-0.2.min.js';
    var entry = document.getElementsByTagName('script')[0];
    entry.parentNode.insertBefore(script, entry);
})();

var ILPSLogging_ready = ILPSLogging_ready || [];

ILPSLogging_ready.push(function() {
    var config = {
        api_url: 'http://ilpslogging.staging.dispectu.com',
        project_key: 'YA9kuhU2zgzQlLl43qCOz66ZAEysJsDHFMFMIidZG7E',
        log_mouse_movements: true,
        log_mouse_clicks: true,
        post_events_queue_on_browser_close: true,
        log_browser_close: true,
        debug: false
    };
    ILPSLogging.init(config, function() {
        console.log('ready for action');
    });
});
