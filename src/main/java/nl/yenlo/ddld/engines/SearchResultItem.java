package nl.yenlo.ddld.engines;

/**
 * The mimimum properties a search result should have.
 * 
 * @author Philipp Gayret
 *
 */
public interface SearchResultItem {

	public String getUrl();

	public String getTitle();

	public String getContent();

}
