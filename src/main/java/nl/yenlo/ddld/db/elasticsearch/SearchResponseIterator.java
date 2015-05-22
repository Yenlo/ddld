package nl.yenlo.ddld.db.elasticsearch;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.elasticsearch.action.search.SearchResponse;

import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Used to allow implementation of custom {@link SearchResponse} items on the iterator.
 *
 * @author Philipp Gayret
 *
 * @param <T>
 */
public interface SearchResponseIterator<T> extends Iterator<T> {

	@JsonIgnore
	public long getTotalHits();

	void setParseType(Type type);

}
