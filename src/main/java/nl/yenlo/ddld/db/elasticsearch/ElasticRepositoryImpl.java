package nl.yenlo.ddld.db.elasticsearch;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.primitives.Ints;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link ElasticRepository}.
 * <p/>
 * Can even use system properties:
 * - ELASTICSEARCH_HOST ( default localhost )
 * - ELASTICSEARCH_HOST_PORT ( default 9300 )
 * - ELASTICSEARCH_SIZE_LIMIT ( default 100 )
 * - ELASTICSEARCH_FIELD_NAMING_POLICY ( default {@link FieldNamingPolicy#LOWER_CASE_WITH_UNDERSCORES} )
 * <p/>
 * Uses {@link Gson} for (de)serializing objects, so you can annotate your models with the {@link Gson} annotations and it should pick them up.
 *
 * @author Philipp Gayret
 */
public class ElasticRepositoryImpl<T> implements ElasticRepository<T> {

    private final Client client;
    private final Type type;
    private final String searchIndex;
    private final String searchType;
    private final Gson gson;

    private static final Logger LOG = LoggerFactory.getLogger(ElasticRepositoryImpl.class);
    private static final String ELASTICSEARCH_HOST = System.getProperty("ELASTICSEARCH_HOST", "localhost");
    private static final Integer ELASTICSEARCH_HOST_PORT = Ints.tryParse(System.getProperty("ELASTICSEARCH_HOST_PORT", "9300"));
    private static final Integer ELASTICSEARCH_SIZE_LIMIT = Ints.tryParse(System.getProperty("ELASTICSEARCH_SIZE_LIMIT", "100"));
    private static final FieldNamingPolicy ELASTICSEARCH_FIELD_NAMING_POLICY = Enum.valueOf(FieldNamingPolicy.class, System.getProperty("ELASTICSEARCH_FIELD_NAMING_POLICY", FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.name()));

    public ElasticRepositoryImpl(String searchIndex, String searchType, Class<? extends T> type) {
        this.client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(ELASTICSEARCH_HOST, ELASTICSEARCH_HOST_PORT));
        this.type = TypeToken.of(type).getType();
        this.gson = new GsonBuilder().setFieldNamingStrategy(ELASTICSEARCH_FIELD_NAMING_POLICY).create();
        this.searchIndex = searchIndex;
        this.searchType = searchType;
    }

    @Override
    public void save(T item, String id) {
        try {
            String result = gson.toJson(item);
            client.prepareIndex(searchIndex, searchType, id).setSource(result).execute().actionGet();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize a given object", e);
        }
    }

    @Override
    public void delete(String id) {
        client.prepareDelete(searchIndex, searchType, id).execute().actionGet();
    }

    @Override
    public void save(T item) {
        this.save(item, null);
    }

    @Override
    public T get(String id) {
        try {
            GetResponse response = client.prepareGet(searchIndex, searchType, id).setOperationThreaded(false).execute().actionGet();
            String result = response.getSourceAsString();
            if (result == null) {
                return null;
            }
            try {
                return gson.fromJson(result, type);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse result to " + type, e);
            }
        } catch (IndexMissingException e) {
            LOG.warn("Missing index: {}, type: {}. Insert data into ElasticSearch and this warning will disappear.", searchIndex, searchType);
            return null;
        }
    }

    @Override
    public SearchResponseIterator<? extends T> search() {
        return this.search(QueryBuilders.matchAllQuery());
    }

    @Override
    public SearchResponseIterator<? extends T> search(QueryBuilder query) {
        return this.search(query, 0, ELASTICSEARCH_SIZE_LIMIT);
    }

    @Override
    public SearchResponseIterator<? extends T> search(QueryBuilder query, int from, int size) {
        try {
            SearchResponse response = client.prepareSearch(searchIndex).setQuery(query).setFrom(from).setSize(size).execute().actionGet();
            LOG.debug("{} hits for a query", response.getHits().getHits().length);
            return new SearchResponseIteratorImpl<T>(gson, response, type);
        } catch (IndexMissingException e) {
            return new EmptySearchResponseIterator<T>();
        }
    }

    /**
     * Note: The field parameter is inserted directly into the update script; make sure the parameter is sanitized.
     *
     * @param id    the id
     * @param field the field
     * @param value the value
     */
    @Override
    public void update(String id, String field, Object value) {
        Map<String, Object> updateObject = new HashMap<String, Object>();
        updateObject.put(field, value);
        client.prepareUpdate(this.searchIndex, this.searchType, id).setScript("ctx._source." + field + "=" + field).setScriptParams(updateObject).execute().actionGet();
    }

}
