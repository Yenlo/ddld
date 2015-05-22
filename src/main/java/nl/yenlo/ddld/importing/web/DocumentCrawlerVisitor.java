package nl.yenlo.ddld.importing.web;

import java.util.Calendar;
import java.util.regex.Pattern;

import nl.yenlo.ddld.importing.processing.Boilerpipe;
import nl.yenlo.ddld.importing.processing.MatchingUtils;
import nl.yenlo.ddld.model.search.CrawlerDocument;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * A {@link WebCrawler} implementation that processes pages with {@link Boilerpipe} and then stores them in the current {@link CrawlerDocumentSink}.
 * 
 * @author Philipp Gayret
 *
 */
public class DocumentCrawlerVisitor extends WebCrawler {

	private static final Pattern EXCLUDES = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	private static final Boilerpipe boilerpipe = new Boilerpipe();

	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		return (!EXCLUDES.matcher(href).matches()) && url.getURL().startsWith("http");
	}

	@Override
	public void visit(Page page) {
		DocumentCrawler documentCrawler = (DocumentCrawler) this.getMyController().getCustomData();
		CrawlerDocumentSink documentSink = documentCrawler.getDocumentSink();
		String url = page.getWebURL().getURL();
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			CrawlerDocument document = new CrawlerDocument();
			document.setSource(documentCrawler.getSourceIdentifier());
			document.setId(documentCrawler.getSourceIdentifier() + ":" + page.getWebURL().toString());
			document.setUrl(url);
			document.setTitle(htmlParseData.getTitle());
			document.setTimestamp(Calendar.getInstance().getTimeInMillis());
			try {
				String text = boilerpipe.getText(htmlParseData.getHtml());
				document.setContent(text);
				document.setContentHash(MatchingUtils.getSubsetHashcode(document));
				documentSink.storeDocument(document);
			} catch (BoilerpipeProcessingException e) {
				e.printStackTrace();
			}

		}
	}

}