$('#search').typeahead({
    source: function (query, process) {
        var url = $('#search').attr('data-url') + '/_msearch';
        var suggest_query = create_suggestion_query(query);
        var succes = function (mresult) {
            var suggestions = suggestion_list_from_query_result(mresult);

            return process(suggestions);
        };

        return $.post(url, suggest_query, succes, 'json');
    },

    updater: function(item) {
        var query = item.replace(/^[^:]*:/, '');
        /* stand-alone typeahead only... */
        // $('#search').typeahead('setQuery', query);

        query_es(query);
    },

    matcher: function(item) {
        /* I don't want Bootstrap to touch the ElasticSearch supplied
         * list of suggestions. */
        return true;
    },

    highlighter: function(item) {
        var query = this.query.replace(/[\-\[\]{}()*+?.,\\\^$|#\s]/g, '\\$&')

        var res = item.replace(new RegExp('(' + query + ')', 'ig'), function ($1, match) {
            return '<strong>' + match + '</strong>'
        });

        return res;
    },

    sorter: function(items) {
        /* FFS don't touch the results... */
        return items;
    }
});
