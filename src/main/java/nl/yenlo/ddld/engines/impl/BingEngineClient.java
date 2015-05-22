package nl.yenlo.ddld.engines.impl;

import nl.yenlo.ddld.engines.BasicSearchResultItem;
import nl.yenlo.ddld.engines.SearchResult;
import nl.yenlo.ddld.importing.exceptions.ImportException;
import org.apache.axiom.om.OMElement;
import org.apache.http.client.methods.HttpGet;

import java.util.*;

/**
 * Client to the Bing HTTP XML API.
 * <p/>
 * {@see http://datamarket.azure.com/dataset/bing/search}
 *
 * @author Philipp Gayret
 */
public class BingEngineClient extends AbstractXMLSearchEngine {

    public static final String API_KEY = "${yenlo.keys.api.bing}";
    public static final String SOURCE_WEB = "Web";
    public static final String SOURCE_NEWS = "News";

    private final String source;

    /**
     * @param source "Web", "News", or any of the other sources the Bing Search API provides.
     */
    public BingEngineClient(String source) {
        super();
        this.source = source;
    }

    /**
     * Queries Bing.
     *
     * @param query the query string
     * @return the result result
     */
    @Override
    public SearchResult search(String query, int max, int offset) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Query", "'" + query + "'");
        params.put("$skip", offset);
        params.put("$top", max);
        try {
            return this.get("https://api.datamarket.azure.com/Bing/Search/v1/" + source, params);
        } catch (ImportException e) {
            return SearchResult.ERRED;
        }
    }

    @Override
    public void prepare(HttpGet httpget) {
        // Basic authentication using username empty and our Bing API key as password.
        httpget.addHeader("Authorization", API_KEY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResult process(OMElement element) {
        Iterator<OMElement> entries = element.getChildrenWithLocalName("entry");
        List<BasicSearchResultItem> items = new ArrayList<BasicSearchResultItem>();
        while (entries.hasNext()) {
            OMElement entry = entries.next();
            OMElement content = (OMElement) entry.getChildrenWithLocalName("content").next();
            OMElement properties = (OMElement) content.getChildrenWithLocalName("properties").next();
            String url = ((OMElement) properties.getChildrenWithLocalName("Url").next()).getText();
            String title = ((OMElement) properties.getChildrenWithLocalName("Title").next()).getText();
            String preview = ((OMElement) properties.getChildrenWithLocalName("Description").next()).getText();
            BasicSearchResultItem item = new BasicSearchResultItem();
            item.setContent(preview);
            item.setTitle(title);
            item.setUrl(url);
            items.add(item);
        }
        // Bing does not supply the estimated hits, so return -1 here to indicate N/A.
        return new SearchResult(items.iterator(), -1L);
    }

}
