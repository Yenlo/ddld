package nl.yenlo.ddld.engines;

/**
 * A very basic class for a structure that all the search engine clients can return.
 * 
 * @author Philipp Gayret
 *
 */
public class BasicSearchResultItem implements SearchResultItem {

	private String url;
	private String title;
	private String content;

	@Override
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
