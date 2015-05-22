package nl.yenlo.ddld.engines;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A very basic class for a structure that all the search engine clients can return.
 * 
 * @author Philipp Gayret
 *
 */
public class SearchResult {

	public static final SearchResult ERRED = new SearchResult(new ArrayList<SearchResultItem>().iterator(), -1L);

	private Iterator<? extends SearchResultItem> items;
	private Long estimatedHits;

	public SearchResult(Iterator<? extends SearchResultItem> items, Long estimatedHits) {
		super();
		this.items = items;
		this.estimatedHits = estimatedHits;
	}

	public Iterator<? extends SearchResultItem> getItems() {
		return this.items;
	}

	public void setItems(Iterator<? extends SearchResultItem> items) {
		this.items = items;
	}

	public Long getEstimatedHits() {
		return this.estimatedHits;
	}

	public void setEstimatedHits(Long estimatedHits) {
		this.estimatedHits = estimatedHits;
	}

}
