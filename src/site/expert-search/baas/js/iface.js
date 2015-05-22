function update_search(results) {
    update_related(results);
    update_experts(results);
};


function update_topic_suggestions(results) {
    var ul = document.getElementById('topics');
    ul.innerHTML = '';
    ul.appendChild(document.createTextNode("weet niet wat je zoekt?"));

    var terms = results.topic_suggest[0].options
    var sorted_terms = terms.sort(function(a,b){return b.total - a.total});

    for (var i in sorted_terms) {
        var term = terms[i].text;
        var score = terms[i].score;
        var li = document.createElement('li');
        var score_span = document.createElement('span');
        var score_text = document.createTextNode(score);
        var text = document.createTextNode(term + " ");
        score_span.appendChild(score_text);
        li.appendChild(text);
        li.appendChild(score_span);
        ul.appendChild(li);
    }
};


function update_author_suggestions(results) {
    var ul = document.getElementById('authors');
    ul.innerHTML = '';
    ul.appendChild(document.createTextNode("weet niet wie je zoekt?"));

    var terms = results.author_suggest[0].options
    var sorted_terms = terms.sort(function(a,b){return b.total - a.total});

    for (var i in sorted_terms) {
        var term = terms[i].text;
        var score = terms[i].score;
        var li = document.createElement('li');
        var text = document.createTextNode(score + " " + term);
        li.appendChild(text);
        ul.appendChild(li);
    }
};


function update_experts(result) {
    var ul = document.getElementById('results');
    ul.innerHTML = '';
    ul.appendChild(document.createTextNode("we kennen de volgende experts:"));

    var terms = result.facets.authors_facet.terms;
    var sorted_terms = terms.sort(function(a,b){return b.total - a.total});

    /* log query result */
    ILPSLogging.queryResults(sorted_terms, sorted_terms.length, sorted_terms.length, result.took);

    for (var i in sorted_terms) {
        var term = terms[i].term;
        var score = terms[i].total.toFixed(3);
        var li = document.createElement('li');
        var str = "<li><span>(" + score + ")</span> " + term + "</li>";
        li.innerHTML = str;
        ul.appendChild(li);
    }
};

function update_related(result) {
    var ul = document.getElementById('related');
    ul.innerHTML = '';
    ul.appendChild(document.createTextNode("wil je misschien een onderwerp als:"));

    var terms = result.facets.topics_facet.terms;
    // var sorted_terms = terms.sort(function(a,b){return b.total - a.total});

    /* log query result */
    ILPSLogging.queryResults(terms, terms.length, terms.length, result.took);

    for (var i in terms) {
        var term = terms[i].term;
        var score = terms[i].count;
        var li = document.createElement('li');
        var str = "<li><span>(" + score + ")</span> " + term + "</li>";
        li.innerHTML = str;
        ul.appendChild(li);
    }
};


function query_es(query_str) {
    var facet_size = 15;
    var query = {
        "query": {
            "function_score": {
                "functions": [
                    {
                        "exp": {
                            "date": {
                                "scale": "360d",
                                "decay": "0.75"
                            }
                        }
                    }
                ],
                "query": {
                    "query_string": {
                        "default_operator": "AND",
                        "query": query_str
                    }
                },
                "score_mode": "multiply"
            }
        },
        "size": 0, /* we don't want the hits, only the facets */
        "facets": {
            "authors_facet": {
                "terms_stats": {
                    "key_field": "authors.untouched",
                    "value_script": "doc.score",
                    "size": facet_size
                }
            },
            "topics_facet": {
                "terms": {
                    "field": "topics.untouched",
                    "size": facet_size
                }
            }
        }
    };

    /* log query */
    ILPSLogging.query(query_str, query);


    $.ajax({
        url: $('#search').attr('data-url') + '/documents_test/_search',
        data: {source: JSON.stringify(query)},
        dataType: 'jsonp',
        success: update_search
    });
};

function suggest_topics(query_str) {
    var query = {
    "topic_suggest" : {
        "text" : query_str,
        "completion" : {
            "field" : "suggest"
            ,
            "fuzzy": {
                "edit_distance": 1
            },
            "size": 10
        }
    }
}

    $.ajax({
        url: $('#search').attr('data-url') + '/topics_test/_suggest',
        data: {source: JSON.stringify(query)},
        dataType: 'jsonp',
        success: update_topic_suggestions
    });
};


function suggest_authors(query_str) {
    var query = {
    "author_suggest" : {
        "text" : query_str,
        "completion" : {
            "field" : "suggest"
            ,
            "fuzzy": {
                "edit_distance": 1
            },
            "size": 10
        }
    }
}

    $.ajax({
        url: $('#search').attr('data-url') + '/authors_test/_suggest',
        data: {source: JSON.stringify(query)},
        dataType: 'jsonp',
        success: update_author_suggestions
    });
};


function query_es_wrap(ev) {
    if (! ev) {
        return false;
    }

    var input = $(ev.target);
    var query_str = input.val();

    query_es(query_str);
};

function suggest_topics_wrap(ev) {
    if (! ev) {
        return false;
    }

    var input = $(ev.target);
    var query_str = input.val();

    suggest_topics(query_str);
};

function suggest_authors_wrap(ev) {
    if (! ev) {
        return false;
    }

    var input = $(ev.target);
    var query_str = input.val();

    suggest_authors(query_str);
};


function create_suggestion_query(query_str, n_suggestions) {
    /* Create an ES multi-query to obtain suggestions for `query_str`.
     *
     * returns the multi-query.
     */
    var url = $('#search').attr('data-url') + '/_msearch';

    /* ES multi search example
       {"index" : "test"}
       {"query" : {"match_all" : {}}, "from" : 0, "size" : 10}
       {"index" : "test", "search_type" : "count"}
       {"query" : {"match_all" : {}}}
    */

    // var n_suggestions = n_suggestions || 3;
    var n_suggestions = 3;
    var suggestion_ind = [
        {
            "topics_test": {
                "topics_test" : {
                    "text" : query_str,
                    "completion" : {
                        "field" : "suggest",
                        "fuzzy" : {
                            "edit_distance": 1
                        },
                        "size": n_suggestions
                    }
                }
            }
        },
        {
            "authors_test": {
                "authors_test" : {
                    "text" : query_str,
                    "completion" : {
                        "field" : "suggest",
                        "fuzzy" : {
                            "edit_distance": 1
                        },
                        "size": n_suggestions
                    }
                }
            }
        }
    ];

    var msearch = "";
    for (var i in suggestion_ind) {
        var field_name = Object.keys(suggestion_ind[i])[0];
        var suggest_query = suggestion_ind[i][field_name];
        msearch = msearch + JSON.stringify({"index": field_name}) + "\n";
        msearch = msearch + JSON.stringify({"suggest": suggest_query}) + "\n";
    }

    /* log suggestion query */
    ILPSLogging.query(query_str, msearch);

    return msearch;
};


function suggestion_list_from_query_result(mresult) {
    /* Convert an ES multi-query suggestion result into a list of
     * suggestions.
     *
     * returns a (possibly empty) list of suggestions (strings)
     */
    if (! mresult) {
        return false;
    }

    var search_time = 0;
    for (var i in mresult.responses) {
        suggest_search_time = mresult.responses[i].took;
        search_time += suggest_search_time;
    }

    var res = [];

    for (var i in mresult.responses) {
        var suggest = mresult.responses[i].suggest;
        var field_name = Object.keys(suggest)[0];
        var suggestions = suggest[field_name][0].options;
        for (var j in suggestions) {
            /* UGH: hack */
            var short_name = field_name.replace(/s_test$/, '');

            res.push(short_name + ":" + suggestions[j].text);
        }
    }

    res.push("search:" + $('#search').val());

    /* log query result */
    ILPSLogging.queryResults(res, res.length, res.length, search_time);

    return res;
};


// $('#search').on('keyup', query_es_wrap);
// $('#topic_suggest').on('keyup', suggest_topics_wrap)
// $('#author_suggest').on('keyup', suggest_authors_wrap)

// $(document).ready($('#search').keyup());
// $(document).ready($('#topic_suggest').keyup());
// $(document).ready($('#author_suggest').keyup());
