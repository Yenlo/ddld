package nl.yenlo.ddld.db.elasticsearch;

import org.elasticsearch.index.query.QueryBuilder;

/**
 * Interface for an abstraction layer on top of the ElasticSearch Java API.
 *
 * @author Philipp Gayret
 */
public interface ElasticRepository<T> {

    /**
     * Not only saves something, it also attaches an id to it, so you can get it out again with {@link #get(String)}
     *
     * @param id the id
     * @param item the item
     */
    public void save(T item, String id);

    /**
     * Deletes an entry with a given id.
     *
     * @param id the id
     */
    public void delete(String id);

    /**
     * {@link #save(Object, String)} without an id.
     *
     * @param item the item
     */
    public void save(T item);

    /**
     * Retrieves an item created via {@link #save(Object, String)}.
     *
     * @param id the id
     * @return the object, or null
     */
    public T get(String id);

    /**
     * @return {@link #search(QueryBuilder)}  with a match-all query
     */
    public SearchResponseIterator<? extends T> search();

    /**
     * Searches with a custom query.
     *
     * @param query the query
     * @return iterator over the resultset
     */
    public SearchResponseIterator<? extends T> search(QueryBuilder query);

    /**
     * Searches with a custom query, offset and limit.
     *
     * @param query the query
     * @return iterator over the resultset
     */
    SearchResponseIterator<? extends T> search(QueryBuilder query, int from, int size);

    /**
     * Updates a document's field to a new value
     *
     * @param id the id
     * @param field the field
     * @param value the value
     */
    void update(String id, String field, Object value);

}