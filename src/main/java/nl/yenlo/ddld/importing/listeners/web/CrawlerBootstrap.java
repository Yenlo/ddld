package nl.yenlo.ddld.importing.listeners.web;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import nl.yenlo.ddld.db.CrawlerDAO;
import nl.yenlo.ddld.db.DocumentDAO;
import nl.yenlo.ddld.db.impl.CrawlerDAOImpl;
import nl.yenlo.ddld.importing.processing.MatchingUtils;
import nl.yenlo.ddld.importing.web.CrawlerDocumentSink;
import nl.yenlo.ddld.importing.web.DocumentCrawler;
import nl.yenlo.ddld.model.Crawler;
import nl.yenlo.ddld.model.search.CrawlerDocument;

import org.apache.log4j.Logger;

/**
 * Polls the database for scheduled {@link Crawler}s to run, and runs them.
 * 
 * @author Philipp Gayret
 *
 */
public class CrawlerBootstrap implements ServletContextListener, Runnable {

	private static final Logger logger = Logger.getLogger(CrawlerBootstrap.class.getName());

	private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
	private static final int THREADS_PER_CRAWLER = 5;
	private final CrawlerDAO crawlerDAO = new CrawlerDAOImpl();
	private final DocumentDAO documentDb = DocumentDAO.getInstance();

	/**
	 * Passes everything on to {@link CrawlerBootstrap#documentDb}, highlighting the ones that match.
	 */
	public class HighlightingCrawlerDocumentSink implements CrawlerDocumentSink {

		private final Collection<String> matchables;

		private HighlightingCrawlerDocumentSink(Collection<String> matchables) {
			this.matchables = matchables;
		}

		@Override
		public void storeDocument(CrawlerDocument basicDocument) {
			boolean match = false;
			for (String matchable : matchables) {
				String normalized = MatchingUtils.normalize(matchable);
				if (basicDocument.getContent().contains(normalized)) {
					match = true;
					break;
				}
			}
			basicDocument.setHighlighted(match);
			CrawlerBootstrap.this.documentDb.storeDocument(basicDocument);
		}

	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		// schedule ourselves to be started every 30 seconds
		scheduledExecutorService.scheduleAtFixedRate(this, 5, 30, TimeUnit.SECONDS);
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
	}

	@Override
	public void run() {
		Collection<Crawler> crawlers = crawlerDAO.getScheduled();
		// first make sure they all get rescheduled first
		for (Crawler crawler : crawlers) {
			logger.info("Rescheduling crawler id=" + crawler.getId() + ", title=" + crawler.getTitle());
			crawler.reschedule();
			crawlerDAO.save(crawler);
		}
		// then initiate crawlers
		for (final Crawler crawler : crawlers) {
			logger.info("Initiating crawler id=" + crawler.getId() + ", title=" + crawler.getTitle());
			HighlightingCrawlerDocumentSink documentSink = new HighlightingCrawlerDocumentSink(crawler.getCrawlFilterArray());
			DocumentCrawler documentCrawler = new DocumentCrawler(CrawlerDocument.getSourceIdentifier(crawler), documentSink);
			documentCrawler.start(crawler.getCrawlRoot(), crawler.getCrawlDepth(), THREADS_PER_CRAWLER);
		}
	}

}