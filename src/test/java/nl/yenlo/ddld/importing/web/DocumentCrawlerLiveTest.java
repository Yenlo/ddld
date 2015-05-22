package nl.yenlo.ddld.importing.web;

import nl.yenlo.ddld.model.search.CrawlerDocument;

/**
 * 
 * @author Philipp Gayret
 *
 */
public class DocumentCrawlerLiveTest {

	// @Test
	public void test() {
		DocumentCrawler documentCrawler = new DocumentCrawler("Test", new CrawlerDocumentSink() {
			@Override
			public void storeDocument(CrawlerDocument basicDocument) {
				System.out.println(basicDocument.getContent());
			}
		});
		documentCrawler.start("http://www.yenlo.nl/nl/", 1, 10);
	}

}
