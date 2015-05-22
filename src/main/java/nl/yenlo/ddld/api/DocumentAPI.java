package nl.yenlo.ddld.api;

import com.google.common.reflect.TypeToken;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import nl.yenlo.ddld.api.exception.FunctionalException;
import nl.yenlo.ddld.db.DocumentDAO;
import nl.yenlo.ddld.db.NoMatchException;
import nl.yenlo.ddld.db.SourceDAO;
import nl.yenlo.ddld.db.elasticsearch.SearchResponseIterator;
import nl.yenlo.ddld.db.impl.SourceDAOImpl;
import nl.yenlo.ddld.engines.Article;
import nl.yenlo.ddld.engines.SearchEngineClient;
import nl.yenlo.ddld.engines.SearchResult;
import nl.yenlo.ddld.engines.impl.BingEngineClient;
import nl.yenlo.ddld.engines.impl.NewzEngineClient;
import nl.yenlo.ddld.importing.processing.Boilerpipe;
import nl.yenlo.ddld.importing.processing.MatchingUtils;
import nl.yenlo.ddld.model.Factcheck;
import nl.yenlo.ddld.model.Source;
import nl.yenlo.ddld.model.User;
import nl.yenlo.ddld.model.search.BasicDocument;
import nl.yenlo.ddld.model.search.RequestDocument;
import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;

import javax.ws.rs.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * This is where documents are indexed, HTML is converted and searches are handled.
 *
 * @author Philipp Gayret
 */
@Path("/document/")
public class DocumentAPI implements SearchEngineClient {

    protected static final Logger logger = Logger.getLogger(DocumentAPI.class.getName());

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private final DocumentDAO documentDb = DocumentDAO.getInstance();
    private final NewzEngineClient newz = new NewzEngineClient(NewzEngineClient.NEWZ_PRODUCTION_ENVIRONMENT, NewzEngineClient.NEWZ_PRODUCTION_AUTHORIZATION);
    private final SearchEngineClient bingWeb = new BingEngineClient(BingEngineClient.SOURCE_WEB);
    private final SearchEngineClient bingNews = new BingEngineClient(BingEngineClient.SOURCE_NEWS);

    private final SourceDAO sourceDao = new SourceDAOImpl();

    public static class FactcheckProcessable {
        public String key;
        public String url;
        public String title;
        public String content;
        public String parentId;
    }

    /**
     * Processes a {@link FactcheckProcessable}, when successful returns a map with an `id` property.
     *
     * @param processable
     * @return { id : "UUID string" }
     */
    @POST
    @Path("/process")
    @Consumes("application/json")
    @Produces("application/json")
    public Map<String, ?> processHTML(final FactcheckProcessable processable) {
        final String uuid = UUID.randomUUID().toString();
        final User user = AuthenticationFilter.getUser();
        final Factcheck factcheck = user.getActiveFactcheckOrErr();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestDocument document = new RequestDocument();
                    document.setId(uuid);
                    document.setSource("User:" + Integer.toString(user.getId()));
                    document.setParentId(processable.parentId);
                    document.setDbFactcheckId(factcheck.getId());
                    document.setUrl(processable.url);
                    document.setTitle(processable.title);
                    document.setTimestamp(Calendar.getInstance().getTimeInMillis());
                    Boilerpipe boilerpipe = new Boilerpipe();
                    String text = boilerpipe.getText(processable.content);
                    document.setContent(text);
                    document.setContentHash(MatchingUtils.getSubsetHashcode(document));
                    documentDb.storeRequestDocument(document);
                } catch (BoilerpipeProcessingException e) {
                    logger.error("Unable to boilerpipe a request, url=" + processable.url, e);
                }
            }
        });
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", uuid);
        return map;
    }

    /**
     * @return list of available search engines
     */
    @GET
    @Path("/sources")
    @Produces("application/json")
    public List<String> sources() {
        return Arrays.asList("newz", "bing-web", "bing-news", "dld");
    }

    /**
     * Processes search requests and forwards them to the appropriate {@link SearchEngineClient}.
     *
     * @param engine search engine to use, see cases below
     * @param query  search query string
     * @param max    maximum results, for pagination
     * @param offset offset, for pagination
     * @return search results
     */
    @GET
    @Path("/search")
    @Produces("application/json")
    public SearchResult search(@QueryParam("engine") String engine, @QueryParam("query") String query, @QueryParam("max") Integer max, @QueryParam("offset") Integer offset) {
        String trueQuery = query.toLowerCase();
        Integer trueMax = Math.min(100, max);
        AuthenticationFilter.getUser();
        switch (engine) {
            case "newz":
                return newz.search(trueQuery, trueMax, offset);
            case "bing-web":
                return bingWeb.search(trueQuery, trueMax, offset);
            case "bing-news":
                return bingNews.search(trueQuery, trueMax, offset);
            case "dld":
                return this.search(trueQuery, trueMax, offset);
            default:
                throw new FunctionalException(FunctionalException.Kind.UNKNOWN_SEARCH_ENGINE);
        }
    }

    /**
     * Retrieves an {@link Article} out of a given engine.
     *
     * @param id     article id
     * @param engine engine to use
     * @return article
     */
    @GET
    @Path("/article")
    @Produces("application/json")
    public Article article(@QueryParam("id") String id, @QueryParam("engine") String engine) {
        AuthenticationFilter.getUser();
        switch (engine) {
            case "newz":
                return newz.get(id);
            default:
                throw new FunctionalException(FunctionalException.Kind.UNKNOWN_ARTICLE_SOURCE);
        }
    }

    /**
     * {@link DocumentAPI} is its own {@link SearchEngineClient} to the ElasticSearch indexed user data.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public SearchResult search(String query, int max, int offset) {
        String trueQuery = query.toLowerCase();
        User user = AuthenticationFilter.getUser();
        Integer factcheckId = user.getActiveFactcheckOrErr().getId();
        BoolQueryBuilder qb = boolQuery().must(termQuery("db_factcheck_id", factcheckId));
        if (trueQuery.length() > 0) {
            qb = qb.must(termQuery("content", trueQuery));
        }
        SearchResponseIterator<? extends BasicDocument> docs = documentDb.search(qb, offset, max);
        // Change the parsing type
        docs.setParseType(TypeToken.of(RequestDocument.class).getType());
        return new SearchResult(docs, docs.getTotalHits());
    }

    public static class Importable {
        public String source;
        public String secret;
        public String title;
        public String content;
        public String url;
    }

    @POST
    @Path("/import")
    @Consumes("application/json")
    @Produces("application/json")
    public void importDocument(Importable importable) {
        try {
            Source source = sourceDao.get(importable.source);
            // very simple authentication, so that not anyone that knows the source name can perform an import
            // it should be obvious that this should only be done over https
            if (source.getSecret().equals(importable.secret)) {
                BasicDocument document = new BasicDocument();
                document.setSource("Source:" + Integer.toString(source.getId()));
                document.setContent(importable.content);
                document.setTitle(importable.title);
                document.setUrl(importable.url);
                document.setTimestamp(Calendar.getInstance().getTimeInMillis());
                document.setContentHash(MatchingUtils.getSubsetHashcode(document));
                documentDb.save(document);
            } else {
                throw new FunctionalException(FunctionalException.Kind.IMPORT_SECRET_UNKNOWN);
            }
        } catch (NoMatchException e) {
            throw new FunctionalException(FunctionalException.Kind.IMPORT_SOURCE_UNKNOWN);
        }
    }

    @GET
    @Path("/highlight/{id}/{state}")
    public void highlight(@PathParam("id") String id, @PathParam("state") Boolean state) {
        documentDb.update(id, "highlighted", state);
    }

    @GET
    @Path("/remove/{id}")
    public void remove(@PathParam("id") String id) {
        documentDb.delete(id);
    }

}
