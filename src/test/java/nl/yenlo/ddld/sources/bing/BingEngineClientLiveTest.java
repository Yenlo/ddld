package nl.yenlo.ddld.sources.bing;

import junit.framework.Assert;
import nl.yenlo.ddld.engines.SearchResult;
import nl.yenlo.ddld.engines.impl.BingEngineClient;

import org.elasticsearch.common.collect.Iterators;

/**
 * 
 * @author Philipp Gayret
 *
 */
public class BingEngineClientLiveTest {

	/**
	 * Runs a query against bing, expecting 50 actual search results.
	 * @throws Throwable
	 */
	// @Test
	public void test() throws Throwable {
		BingEngineClient bingEngineClient = new BingEngineClient(BingEngineClient.SOURCE_WEB);
		SearchResult result = bingEngineClient.search("xbox", 50, 0);
		Assert.assertEquals(50, Iterators.size(result.getItems()));
	}

}
