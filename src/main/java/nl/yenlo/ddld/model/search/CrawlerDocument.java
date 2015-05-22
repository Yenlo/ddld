package nl.yenlo.ddld.model.search;

import nl.yenlo.ddld.model.Crawler;

/**
 * A document created by a crawler, with a highlighted property to indicate a filter match.
 * 
 * @author Philipp Gayret
 *
 */
public class CrawlerDocument extends BasicDocument {

	private Boolean highlighted;

	public static String getSourceIdentifier(Crawler crawler) {
		return "crawler" + crawler.getId();
	}

	public Boolean getHighlighted() {
		return this.highlighted;
	}

	public void setHighlighted(Boolean highlighted) {
		this.highlighted = highlighted;
	}

}