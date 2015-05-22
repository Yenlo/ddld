package nl.yenlo.ddld.importing.web;

import nl.yenlo.ddld.model.search.BasicDocument;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * Initiates {@link DocumentCrawlerVisitor}; Goes to URLs, processes them to {@link BasicDocument}s and stores them in the given {@link CrawlerDocumentSink}.
 * 
 * @author Philipp Gayret
 *
 */
public class DocumentCrawler {

	// on a windows system this will be the root of the drive you're running the application from
	private static final String DOCUMENT_CRAWLER_STORAGE_DIR = System.getProperty("DOCUMENT_CRAWLER_STORAGE_DIR", "/data/crawl/");

	private final CrawlerDocumentSink documentSink;
	private final String sourceIdentifier;

	/**
	 * @param sourceIdentifier source identifier to mark documents with
	 * @param documentSink where to write documents to
	 */
	public DocumentCrawler(String sourceIdentifier, CrawlerDocumentSink documentSink) {
		this.sourceIdentifier = sourceIdentifier;
		this.documentSink = documentSink;
	}

	/**
	 * @param seed initial page to start crawling and getting resources from
	 * @param depth how deep to go, how many links to follow, a good number for this would be 0 or 1, you can go higher but you'll risk indexing too much
	 * @param threads threads to use for indexing
	 */
	public void start(String seed, int depth, int threads) {
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(DOCUMENT_CRAWLER_STORAGE_DIR);
		config.setMaxDepthOfCrawling(depth);
		config.setFollowRedirects(true);
		config.setIncludeHttpsPages(true);
		config.setConnectionTimeout(4000);
		try {
			PageFetcher pageFetcher = new PageFetcher(config);
			RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
			RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			// WebCrawlerImpl uses this `CustomData` to reference back to this object
			controller.setCustomData(this);
			controller.addSeed(seed);
			controller.start(DocumentCrawlerVisitor.class, threads);
		} catch (Exception e) {
			// initiating the CrawlController could have thrown an exception, it will check if it can write to the storage folder
			e.printStackTrace();
		}
	}

	public CrawlerDocumentSink getDocumentSink() {
		return this.documentSink;
	}

	public String getSourceIdentifier() {
		return this.sourceIdentifier;
	}

}