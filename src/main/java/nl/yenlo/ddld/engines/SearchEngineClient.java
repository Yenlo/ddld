package nl.yenlo.ddld.engines;

/**
 * An interface for every search engine clients to implement.
 * 
 * @author Philipp Gayret
 *
 */
public interface SearchEngineClient {

	/**
	 * @param query any query string
	 * @param max maximum amounf of {@link BasicSearchResultItem}s the {@link SearchResult} may contain.
	 * @param offset the offset in the total search set.
	 * @return search result
	 */
	public SearchResult search(String query, int max, int offset);

}
