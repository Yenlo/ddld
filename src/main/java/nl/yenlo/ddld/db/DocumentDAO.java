package nl.yenlo.ddld.db;

import nl.yenlo.ddld.db.elasticsearch.ElasticRepositoryImpl;
import nl.yenlo.ddld.importing.web.CrawlerDocumentSink;
import nl.yenlo.ddld.model.search.BasicDocument;
import nl.yenlo.ddld.model.search.CrawlerDocument;
import nl.yenlo.ddld.model.search.RequestDocument;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.Iterator;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author Philipp Gayret
 */
public class DocumentDAO extends ElasticRepositoryImpl<BasicDocument> implements CrawlerDocumentSink {

    private static final String DOCUMENT_DB_INDEX = "ddld";
    private static final String DOCUMENT_DB_TYPE = "requestdocument";

    private static final DocumentDAO instance = new DocumentDAO(DOCUMENT_DB_INDEX, DOCUMENT_DB_TYPE);

    public static DocumentDAO getInstance() {
        return instance;
    }

    private DocumentDAO(String searchIndex, String searchType) {
        super(searchIndex, searchType, BasicDocument.class);
    }

    /**
     * returns all documents with a similar:
     * - {@link RequestDocument#getContentHash()}
     * - {@link RequestDocument#getDbFactcheckId()}
     * - {@link RequestDocument#getSource()}
     *
     * @return iterator that iterates over the resultset of matches.
     */
    public Iterator<? extends BasicDocument> getRequestDocumentSimilars(RequestDocument requestDocument) {
        BoolQueryBuilder qb = boolQuery();
        qb = qb.must(termQuery("source", requestDocument.getSource()));
        qb = qb.must(termQuery("db_factcheck_id", requestDocument.getDbFactcheckId()));
        qb = qb.must(termQuery("content_hash", requestDocument.getContentHash()));
        return this.search(qb, 0, 1);
    }

    /**
     * returns all documents with a similar:
     * - {@link BasicDocument#getContentHash()}
     * - {@link BasicDocument#getSource()}
     *
     * @return iterator that iterates over the resultset of matches.
     */
    public Iterator<? extends BasicDocument> getBasicDocumentSimilars(BasicDocument requestDocument) {
        BoolQueryBuilder qb = boolQuery();
        qb = qb.must(termQuery("source", requestDocument.getSource()));
        qb = qb.must(termQuery("content_hash", requestDocument.getContentHash()));
        return this.search(qb, 0, 1);
    }

    /**
     * Saves the document if there is no similar document of it already.
     */
    public void storeRequestDocument(RequestDocument requestDocument) {
        if (requestDocument.getContent().length() > 0 && !this.getRequestDocumentSimilars(requestDocument).hasNext()) {
            this.save(requestDocument, requestDocument.getId());
        }
    }

    /**
     * Saves the document if there is no similar document of it already.
     */
    @Override
    public void storeDocument(CrawlerDocument basicDocument) {
        if (basicDocument.getContent().length() > 0 && !this.getBasicDocumentSimilars(basicDocument).hasNext()) {
            this.save(basicDocument, basicDocument.getId());
        }
    }

}
