package nl.yenlo.ddld.db.elasticsearch;

import org.elasticsearch.common.collect.Iterators;

import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * An implementation of {@link nl.yenlo.ddld.db.elasticsearch.SearchResponseIterator} which contains noting.
 *
 * @author Philipp Gayret
 *
 * @param <T>
 */
public class EmptySearchResponseIterator<T> implements SearchResponseIterator<T> {

	private final Iterator<T> inner;

	public EmptySearchResponseIterator() {
		this.inner = Iterators.emptyIterator();
	}

	@Override
	public boolean hasNext() {
		return inner.hasNext();
	}

	@Override
	public T next() {
		return inner.next();
	}

	@Override
	public void remove() {
		inner.remove();
	}

	@Override
	public long getTotalHits() {
		return 0;
	}

	@Override
	public void setParseType(Type type) {
	}

}
