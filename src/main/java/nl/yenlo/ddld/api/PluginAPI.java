package nl.yenlo.ddld.api;

import nl.yenlo.ddld.db.elasticsearch.ElasticRepositoryImpl;
import nl.yenlo.ddld.db.elasticsearch.SearchResponseIterator;
import nl.yenlo.ddld.model.Factcheck;
import nl.yenlo.ddld.model.User;
import nl.yenlo.ddld.model.plugin.Configuration;
import nl.yenlo.ddld.model.plugin.Pageview;
import org.elasticsearch.index.query.BoolQueryBuilder;

import javax.ws.rs.*;
import java.util.UUID;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Allows for storing data that the plugin and the application need to send to eachother.
 * 
 * We need this for security reasons and here is why and how;
 * 
 * The plugin contains, and can only contain, two scripts:
 * [1] A script not connected to a window ( `plugin-background.js` ) which keeps track of all open tabs to calculate a history tree
 * [2] A script per top level window ( `plugin.js` ) which can check the domain it is in
 * The application pages, also running two kinds of scripts:
 * [3] The "normal" DDLD webpage scripts, running in a window they own
 * [4] The iframe `/app/sidebar` which is inserted into trackable windows
 * 
 * This is how they talk to eachother, securely:
 * - [1] and [2] can communicate over Crossrider's messaging API, in combination with Crossrider's async storage db API we can send any kidn of message.
 * - [3] and [4] can communicate over localStorage which is unique to the domain, when we change anything in localStorage it emits an event to all other pages of our domain.
 * - [2] and [3] can communicate over localStorage as the plugin has elevated privileges and can access the [3]'s localStorage
 * 
 * The issue here is that some browsers ( ie. Chrome ) and thus Crossrider do not allow for injection of `plugin.js` into iframes, only top level windows. Thus [4] cannot
 * securely communicate with the plugin itself.
 * 
 * There are insecure ways we could communicate, like change the iframe's url hash to a value and then they could talk, but anyone else would be able to read it. We also
 * cannot use localStorage and then communicate via another window, because that would require everyone to have always open a window of the main application which the users
 * really don't want to have.
 * 
 * However: They can all read localStorage, and the plugin has to be connected at least once with the website to read out the user's credentials so that it can authenticate
 * to our API. Now with these shared credentials we can make requests from all four places, and noone will be able to listen in. As such we can poll for changes from the
 * background plugin, or maybe even do some sort of `Push` style thing where changes are pushed to listeners. This way the user can do his configuration from the sidebar on
 * any page, and the sidebar can then send this change to here. After that the background plugin will automatically pick up these changes. 
 * 
 * @author Philipp Gayret
 */
@Path("/plugin/")
public class PluginAPI {

	private static final String ELASTICSEARCH_PLUGIN_INDEX = "plugin";
	private static final String ELASTICSEARCH_PAGEVIEW_TYPE = "pageview";
	private static final String ELASTICSEARCH_CONFIGURATION_TYPE = "configuration";

	private final ElasticRepositoryImpl<Pageview> pageviewDb = new ElasticRepositoryImpl<Pageview>(ELASTICSEARCH_PLUGIN_INDEX, ELASTICSEARCH_PAGEVIEW_TYPE, Pageview.class);
	private final ElasticRepositoryImpl<Configuration> configurationDb = new ElasticRepositoryImpl<Configuration>(ELASTICSEARCH_PLUGIN_INDEX, ELASTICSEARCH_CONFIGURATION_TYPE, Configuration.class);

	public static class puttablePageview {
		public Long sourceTimestamp;
		public String sourceUrl;
		public Long targetTimestamp;
		public String targetUrl;
	}

	/**
	 * Inserts a {@link Pageview} referencing the {@link User}'s currently active {@link Factcheck}.
	 * 
	 * @param puttablePageview the pageview
	 */
	@POST
	@Path("/pageview")
	@Produces("application/json")
	@Consumes("application/json")
	public void pageviewCreate(puttablePageview puttablePageview) {
		final String id = UUID.randomUUID().toString();
		final User user = AuthenticationFilter.getUser();
		final Factcheck factcheck = user.getActiveFactcheckOrErr();
		Pageview pageview = new Pageview();
		pageview.setId(id);
		pageview.setFactcheckId(factcheck.getId());
		pageview.setUserId(user.getId());
		pageview.setSourceTimestamp(puttablePageview.sourceTimestamp);
		pageview.setSourceUrl(puttablePageview.sourceUrl);
		pageview.setTargetTimestamp(puttablePageview.targetTimestamp);
		pageview.setTargetUrl(puttablePageview.targetUrl);
		this.pageviewDb.save(pageview, id);
	}

	/**
	 * @return all pageviews for the {@link User}'s currently active {@link Factcheck}.
	 */
	@GET
	@Path("/pageview")
	@Produces("application/json")
	public SearchResponseIterator<? extends Pageview> pageviewRetrieve() {
		final User user = AuthenticationFilter.getUser();
		final Factcheck factcheck = user.getActiveFactcheckOrErr();
		BoolQueryBuilder qb = boolQuery();
		qb = qb.must(termQuery("user_id", user.getId()));
		qb = qb.must(termQuery("factcheck_id", factcheck.getId()));
		return this.pageviewDb.search(qb, 0, 1000);
	}

	/**
	 * Inserts a {@link Configuration} unique to the {@link User}.
	 * 
	 * @param configuration te configuration
	 */
	@POST
	@Path("/configuration")
	@Produces("application/json")
	@Consumes("application/json")
	public void configurationCreate(Configuration configuration) {
		final User user = AuthenticationFilter.getUser();
		this.configurationDb.save(configuration, user.getId().toString());
	}

	/**
	 * @return the {@link Configuration} unique to the {@link User}.
	 */
	@GET
	@Path("/configuration")
	@Produces("application/json")
	public Configuration configurationRetrieve() {
		final User user = AuthenticationFilter.getUser();
		return this.configurationDb.get(user.getId().toString());
	}

}
