package nl.yenlo.ddld.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import nl.yenlo.ddld.api.exception.FunctionalException;
import nl.yenlo.ddld.db.CrawlerDAO;
import nl.yenlo.ddld.db.DocumentDAO;
import nl.yenlo.ddld.db.NoMatchException;
import nl.yenlo.ddld.db.elasticsearch.SearchResponseIterator;
import nl.yenlo.ddld.db.impl.CrawlerDAOImpl;
import nl.yenlo.ddld.engines.SearchResult;
import nl.yenlo.ddld.model.Crawler;
import nl.yenlo.ddld.model.Crawler.CrawlFrequencyType;
import nl.yenlo.ddld.model.Crawler.CrawlType;
import nl.yenlo.ddld.model.User;
import nl.yenlo.ddld.model.search.BasicDocument;
import nl.yenlo.ddld.model.search.CrawlerDocument;
import org.elasticsearch.index.query.*;

import javax.ws.rs.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Interface to a {@link User}'s  {@link Crawler}s.
 *
 * @author Philipp Gayret
 */
@Path("/crawler/")
public class CrawlerAPI {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final CrawlerDAO crawlerDao = new CrawlerDAOImpl();
    private final DocumentDAO documentDb = DocumentDAO.getInstance();

    @GET
    @Path("/")
    @Produces("application/json")
    public Collection<Crawler> list() {
        User user = AuthenticationFilter.getUser();
        return crawlerDao.get(user);
    }

    /**
     * We are consuming this format in the method below.
     */
    public static class PuttableCrawler {
        public String title;
        public String root;
        public Integer depth;
        public Boolean external;
        public CrawlType type;
        public CrawlFrequencyType frequencyType;
        public Integer frequency;
        public String filter;
    }

    public void checkPreconditions(PuttableCrawler puttableCrawler) {
        // All these preconditions have the same validation in the frontend; These can never happen via the frontend
        Preconditions.checkArgument(!Strings.isNullOrEmpty(puttableCrawler.title), "De omschrijving is verplicht.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(puttableCrawler.root), "De URL is verplicht.");
        Preconditions.checkArgument(puttableCrawler.depth <= 3, "De maximale depth voor een push-bron is 3, probeer een lager getal.");
        Preconditions.checkNotNull(puttableCrawler.type, "De bestandssoort van documenten om te indexeren is niet opgegeven.");
        Preconditions.checkNotNull(puttableCrawler.frequencyType, "De frequentie is niet aangegeven.");
        Preconditions.checkArgument(puttableCrawler.frequency >= 1, "De crawl frequentie moet minimaal 1 zijn.");
    }

    @POST
    @Path("/")
    @Produces("application/json")
    @Consumes("application/json")
    public void create(PuttableCrawler puttableCrawler) {
        checkPreconditions(puttableCrawler);
        User user = AuthenticationFilter.getUser();
        Crawler crawler = new Crawler();
        crawler.setOwner(user);
        crawler.setTitle(puttableCrawler.title);
        crawler.setCrawlRoot(puttableCrawler.root);
        crawler.setCrawlDepth(puttableCrawler.depth);
        crawler.setCrawlType(puttableCrawler.type);
        crawler.setCrawlExternal(puttableCrawler.external);
        crawler.setCrawlFrequencyType(puttableCrawler.frequencyType);
        crawler.setCrawlFrequency(puttableCrawler.frequency);
        crawler.setCrawlFilter(puttableCrawler.filter);
        crawler.reschedule();
        crawlerDao.save(crawler);
    }

    @GET
    @Path("/{crawlerId}")
    @Produces("application/json")
    public Crawler get(@PathParam("crawlerId") final Integer crawlerId) {
        User user = AuthenticationFilter.getUser();
        try {
            return crawlerDao.get(user, crawlerId);
        } catch (NoMatchException e) {
            throw new FunctionalException(FunctionalException.Kind.CRAWLER_UNKNOWN);
        }
    }

    @POST
    @Path("/{crawlerId}")
    @Produces("application/json")
    @Consumes("application/json")
    public void update(@PathParam("crawlerId") final Integer crawlerId, PuttableCrawler puttableCrawler) {
        checkPreconditions(puttableCrawler);
        User user = AuthenticationFilter.getUser();
        try {
            Crawler crawler = crawlerDao.get(user, crawlerId);
            crawler.setTitle(puttableCrawler.title);
            crawler.setCrawlRoot(puttableCrawler.root);
            crawler.setCrawlDepth(puttableCrawler.depth);
            crawler.setCrawlType(puttableCrawler.type);
            crawler.setCrawlExternal(puttableCrawler.external);
            crawler.setCrawlFrequencyType(puttableCrawler.frequencyType);
            crawler.setCrawlFrequency(puttableCrawler.frequency);
            crawler.setCrawlFilter(puttableCrawler.filter);
            crawler.reschedule();
            crawlerDao.save(crawler);
        } catch (NoMatchException e) {
            throw new FunctionalException(FunctionalException.Kind.CRAWLER_UNKNOWN);
        }
    }

    /**
     * Deletes a crawler and all the documents that come with it; where its db_crawler_id equals the given crawlerId.
     *
     * @param crawlerId the crawler id
     * @throws FunctionalException when no crawler exists for the given crawler id
     */
    @GET
    @Path("/{crawlerId}/delete")
    @Produces("application/json")
    public void delete(@PathParam("crawlerId") final Integer crawlerId) {
        User user = AuthenticationFilter.getUser();
        try {
            Crawler crawler = crawlerDao.get(user, crawlerId);
            crawlerDao.delete(crawler);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    BoolQueryBuilder qb = boolQuery().must(termQuery("source", "crawler:" + crawlerId));
                    Iterator<? extends BasicDocument> docs = documentDb.search(qb);
                    while (docs.hasNext()) {
                        BasicDocument doc = docs.next();
                        documentDb.delete(doc.getId());
                    }
                }
            });
        } catch (NoMatchException e) {
            throw new FunctionalException(FunctionalException.Kind.CRAWLER_UNKNOWN);
        }
    }

    /**
     * Lists all the documents for a User's given crawler by Id, updated or indexed since the given Timestamp.
     *
     * @param crawlerId      the crawler id
     * @param sinceTimestamp a timestamp from which all results will be returned
     * @param max            the maximum amount of resulst, for pagination
     * @param offset         the offset in results, for pagination
     * @return the documents wrapped in a {@link SearchResult}
     */
    @GET
    @Path("/{crawlerId}/documents")
    @Produces("application/json")
    public SearchResult documents(@PathParam("crawlerId") Integer crawlerId, @QueryParam("since") Long sinceTimestamp, @QueryParam("max") Integer max, @QueryParam("offset") Integer offset) {
        User user = AuthenticationFilter.getUser();
        try {
            Integer trueMax = Math.min(20, max);
            Crawler crawler = crawlerDao.get(user, crawlerId);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(sinceTimestamp);
            // construct an elasticsearch query that queries only the crawler's documents, after the given timestamp
            RangeFilterBuilder filter = FilterBuilders.rangeFilter("timestamp").from(sinceTimestamp);
            TermQueryBuilder tb = termQuery("source", CrawlerDocument.getSourceIdentifier(crawler));
            BoolQueryBuilder qb = boolQuery().must(tb);
            FilteredQueryBuilder fqb = QueryBuilders.filteredQuery(qb, filter);
            SearchResponseIterator<? extends BasicDocument> docs = documentDb.search(fqb, offset, trueMax);
            // change the parsing type to CrawlerDocument
            docs.setParseType(TypeToken.of(CrawlerDocument.class).getType());
            return new SearchResult(docs, docs.getTotalHits());
        } catch (NoMatchException e) {
            throw new FunctionalException(FunctionalException.Kind.CRAWLER_UNKNOWN);
        }
    }

    /**
     * Lists only the highlighted documents for a User's given crawler by Id, updated or indexed since the given Timestamp.
     *
     * @param crawlerId      the crawler id
     * @param sinceTimestamp a timestamp from which all results will be returned
     * @param max            the maximum amount of resulst, for pagination
     * @param offset         the offset in results, for pagination
     * @return the documents wrapped in a {@link SearchResult}
     */
    @GET
    @Path("/{crawlerId}/highlights")
    @Produces("application/json")
    public SearchResult highlights(@PathParam("crawlerId") Integer crawlerId, @QueryParam("since") Long sinceTimestamp, @QueryParam("max") Integer max, @QueryParam("offset") Integer offset) {
        User user = AuthenticationFilter.getUser();
        try {
            Integer trueMax = Math.min(20, max);
            Crawler crawler = crawlerDao.get(user, crawlerId);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(sinceTimestamp);
            // construct an elasticsearch query that queries only the crawler's documents, after the given timestamp
            RangeFilterBuilder filter = FilterBuilders.rangeFilter("timestamp").from(sinceTimestamp);
            TermQueryBuilder tb = termQuery("source", CrawlerDocument.getSourceIdentifier(crawler));
            BoolQueryBuilder qb = boolQuery().must(tb);
            qb = qb.must(termQuery("highlighted", true));
            FilteredQueryBuilder fqb = QueryBuilders.filteredQuery(qb, filter);
            SearchResponseIterator<? extends BasicDocument> docs = documentDb.search(fqb, offset, trueMax);
            // change the parsing type to CrawlerDocument
            docs.setParseType(TypeToken.of(CrawlerDocument.class).getType());
            return new SearchResult(docs, docs.getTotalHits());
        } catch (NoMatchException e) {
            throw new FunctionalException(FunctionalException.Kind.CRAWLER_UNKNOWN);
        }
    }

}
