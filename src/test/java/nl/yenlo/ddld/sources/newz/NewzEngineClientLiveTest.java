package nl.yenlo.ddld.sources.newz;

import junit.framework.Assert;
import nl.yenlo.ddld.engines.Article;
import nl.yenlo.ddld.engines.SearchResult;
import nl.yenlo.ddld.engines.impl.NewzEngineClient;

import org.elasticsearch.common.collect.Iterators;

/**
 * 
 * @author Philipp Gayret
 *
 */
public class NewzEngineClientLiveTest {

	private static final String TEST_SEARCH_QUERY = "xbox";
	private static final String TEST_ARTICLE_ID = "urn:5:text:newsml:ADN,20131121:newsml_20131121-89ef34046d919ffd2551b97ba11f630686909895-00000";
	private static final String TEST_ARTICLE_ASSERTION_TITLE = "PlayStation kost 399 euro, Xbox One is 100 euro duurder Strijd nieuwste spelcomputers beslist op prijs";

	/**
	 * Runs a query against newz, expecting 50 actual search results.
	 */
	// @Test
	public void testAsSearchEngienClient() throws Throwable {
		NewzEngineClient newzEngineClient = new NewzEngineClient(NewzEngineClient.NEWZ_PRODUCTION_ENVIRONMENT, NewzEngineClient.NEWZ_PRODUCTION_AUTHORIZATION);
		SearchResult result = newzEngineClient.search(TEST_SEARCH_QUERY, 50, 0);
		Assert.assertEquals(50, Iterators.size(result.getItems()));
	}

	/**
	 * Retrieves an article out of newz, expects it to exist.
	 */
	// @Test
	public void testAsArticleSource() throws Throwable {
		NewzEngineClient newzEngineClient = new NewzEngineClient(NewzEngineClient.NEWZ_PRODUCTION_ENVIRONMENT, NewzEngineClient.NEWZ_PRODUCTION_AUTHORIZATION);
		Article result = newzEngineClient.get(TEST_ARTICLE_ID);
		Assert.assertEquals(TEST_ARTICLE_ASSERTION_TITLE, result.getTitle());
		Assert.assertEquals(8, result.getParagraphs().size());
	}

}
