package nl.yenlo.ddld.engines.impl;

import nl.yenlo.ddld.api.exception.FunctionalException;
import nl.yenlo.ddld.api.exception.FunctionalException.Kind;
import nl.yenlo.ddld.engines.Article;
import nl.yenlo.ddld.engines.ArticleSource;
import nl.yenlo.ddld.engines.BasicSearchResultItem;
import nl.yenlo.ddld.engines.SearchResult;
import nl.yenlo.ddld.importing.exceptions.ImportException;
import org.apache.axiom.om.OMElement;
import org.apache.http.client.methods.HttpGet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Client to the Newz HTTP XML API.
 *
 * @author Philipp Gayret
 */
public class NewzEngineClient extends AbstractXMLSearchEngine implements ArticleSource {

    public static final String NEWZ_API_KEY = "${yenlo.keys.api.bing}";
    public static final String NEWZ_PRODUCTION_AUTHORIZATION = "Basic " + NEWZ_API_KEY;
    public static final String NEWZ_PRODUCTION_ENVIRONMENT = "https://mp.newz.nl/web/api/";

    private final String environment;
    private final String authorization;

    public NewzEngineClient(String environment, String authorization) {
        this.environment = environment;
        this.authorization = authorization;
    }

    @Override
    public void prepare(HttpGet httpget) {
        httpget.addHeader("Authorization", authorization);
    }

    ;

    @SuppressWarnings("unchecked")
    @Override
    public SearchResult process(OMElement element) {
        try {
            Iterator<OMElement> entries = element.getChildrenWithLocalName("articles");
            List<BasicSearchResultItem> items = new ArrayList<BasicSearchResultItem>();
            while (entries.hasNext()) {

                OMElement entry = entries.next();
                String articleTitle = ((OMElement) entry.getChildrenWithLocalName("articleTitle").next()).getText();
                String titleSystem = ((OMElement) entry.getChildrenWithLocalName("titleSystem").next()).getText();
                String id = ((OMElement) entry.getChildrenWithLocalName("id").next()).getText();
                String idEncoded;
                idEncoded = URLEncoder.encode(id, "UTF-8");
                BasicSearchResultItem item = new BasicSearchResultItem();
                item.setContent("");
                item.setTitle(String.format("\"%s\" - %s", articleTitle, titleSystem));
                item.setUrl("/app/article?engine=newz&id=" + idEncoded);
                items.add(item);

            }
            String totals = ((OMElement) element.getChildrenWithLocalName("totalNumberOfMatches").next()).getText();
            return new SearchResult(items.iterator(), Long.parseLong(totals));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is unknown.");
        }
    }

    @Override
    public SearchResult search(String query, int max, int offset) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("q", query);
        params.put("start", offset);
        params.put("count", max);
        try {
            return this.get(environment + "search/articles", params);
        } catch (ImportException e) {
            return SearchResult.ERRED;
        }
    }

    /**
     * @param query de query
     * @param start de start index of the result set
     * @param count the maximum amount of results
     * @return the parsed result
     * @throws ImportException
     */
    public OMElement searchArticles(String query, int start, int count) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (query != null) {
            params.put("q", query);
            params.put("start", start);
            params.put("count", count);
        }
        return this.getRaw(environment + "search/articles", params);
    }

    /**
     * @param id the id of the article
     * @return the parsed nitf xml of the article
     * @throws ImportException
     */
    public OMElement retrieveArticles(String id) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (id != null) {
            params.put("id", id);
        }
        return this.getRaw(environment + "retrieve/articles", params);
    }

    /**
     * @param id het id van het op te halen artikel.
     * @return the parsed attacments
     * @throws ImportException
     */
    public OMElement retrieveArticlesAttachments(String id) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (id != null) {
            params.put("id", id);
        }
        return this.getRaw(environment + "retrieve/articles/attachments", params);
    }

    /**
     * @param uri de uri van het artikel
     * @return the parsed attachments
     * @throws ImportException
     */
    public OMElement retrieveArticlesAttachment(String uri) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (uri != null) {
            params.put("uri", uri);
        }
        return this.getRaw(environment + "retrieve/articles/attachment", params);
    }

    /**
     * @param q the query string
     * @return the parsed concepts
     * @throws ImportException
     */
    public OMElement searchConcepts(String q) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (q != null) {
            params.put("q", q);
        }
        return this.getRaw(environment + "search/concepts", params);
    }

    /**
     * @param id the id
     * @return the parsed concepts
     * @throws ImportException
     */
    public OMElement searchRelatedConcepts(String id) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (id != null) {
            params.put("id", id);
        }
        return this.getRaw(environment + "search/related/concepts", params);
    }

    /**
     * @return the parsed concepts
     * @throws ImportException
     */
    public OMElement searchTopConcepts() throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        return this.getRaw(environment + "search/topconcepts", params);
    }

    /**
     * @param id the id
     * @return the parsed articles
     * @throws ImportException
     */
    public OMElement searchConceptArticles(String id) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (id != null) {
            params.put("id", id);
        }
        return this.getRaw(environment + "search/related/article", params);
    }

    /**
     * @param id the id
     * @return the parsed concept
     * @throws ImportException
     */
    public OMElement searchConceptinfo(String id) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (id != null) {
            params.put("id", id);
        }
        return this.getRaw(environment + "search/conceptinfo", params);
    }

    /**
     * @param id the id
     * @return the parsed concept articles
     * @throws ImportException
     */
    public OMElement searchConceptRelatedArticles(String id) throws ImportException {
        Map<String, Object> params = new HashMap<String, Object>();
        if (id != null) {
            params.put("id", id);
        }
        return this.getRaw(environment + "search/conceptRelatedArticles", params);
    }

    /**
     * Retrieves a Newz article, and converts it to the general {@link Article} structure.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Article get(String id) {
        try {
            OMElement element = this.retrieveArticles(id);
            OMElement head = (OMElement) element.getChildrenWithLocalName("head").next();
            OMElement body = (OMElement) element.getChildrenWithLocalName("body").next();
            OMElement bodyContent = (OMElement) body.getChildrenWithLocalName("body.content").next();
            Iterator<OMElement> paragraphs = bodyContent.getChildrenWithLocalName("p");

            String title = ((OMElement) head.getChildrenWithLocalName("title").next()).getText();
            List<String> paragraphTexts = new ArrayList<String>();
            while (paragraphs.hasNext()) {
                OMElement paragraph = paragraphs.next();
                paragraphTexts.add(paragraph.getText());
            }

            Article article = new Article();
            article.setTitle(title);
            article.setParagraphs(paragraphTexts);

            return article;
        } catch (ImportException e) {
            throw new FunctionalException(Kind.RESOURCE_NOT_FOUND);
        }
    }

}
