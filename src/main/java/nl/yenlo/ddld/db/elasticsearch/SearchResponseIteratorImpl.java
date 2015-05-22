package nl.yenlo.ddld.db.elasticsearch;

import com.google.gson.Gson;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Wraps a string iterator and parses the strings it outputs with Jackson.
 * 
 * @author Philipp Gayret
 *
 * @param <T>
 */
public class SearchResponseIteratorImpl<T> implements SearchResponseIterator<T> {

	private final SearchResponse response;
	private final Iterator<SearchHit> inner;
	private Type type;
	private final Gson gson;

	private static final Logger LOG = LoggerFactory.getLogger(SearchResponseIteratorImpl.class);

	public SearchResponseIteratorImpl(Gson gson, SearchResponse response, Type type) {
		this.response = response;
		this.inner = response.getHits().iterator();
		this.type = type;
		this.gson = gson;
	}

	@Override
	public boolean hasNext() {
		return inner.hasNext();
	}

	@Override
	public void setParseType(Type type) {
		this.type = type;
	}

	@Override
	public T next() {
		String entry = inner.next().getSourceAsString();
		try {
			return gson.fromJson(entry, type);
		} catch (Exception e) {
			LOG.error("Unable to parse an iterator item to [{}] of content [{}]", type, entry, e);
			throw new IllegalArgumentException("Could not parse incoming database data.", e);
		}
	}

	@Override
	public void remove() {
		inner.remove();
	}

	/**
     * Note that this method is annotated with JsonIgnore because because this class has any other properties, it would
     * be serialized by Jackson as a JSON object, as opposed to a JSON array of the contents of this as an Iterator.
     *
	 * @return the total number of hits.
	 */
	@Override
	@JsonIgnore
	public long getTotalHits() {
		return this.response.getHits().getTotalHits();
	}

}
