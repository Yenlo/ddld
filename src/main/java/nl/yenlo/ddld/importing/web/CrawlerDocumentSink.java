package nl.yenlo.ddld.importing.web;

import nl.yenlo.ddld.model.search.CrawlerDocument;

/**
 * A "write-only" place for {@link CrawlerDocument}s to go to.
 * 
 * @author Philipp Gayret
 *
 */
public interface CrawlerDocumentSink {

	public void storeDocument(CrawlerDocument basicDocument);

}
