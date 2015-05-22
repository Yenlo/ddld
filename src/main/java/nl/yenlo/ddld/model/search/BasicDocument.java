package nl.yenlo.ddld.model.search;

import nl.yenlo.ddld.engines.SearchResultItem;
import nl.yenlo.ddld.model.User;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * An ElasticSearch indexed document.
 * 
 * {@link #source} should be a reference to for example a {@link User} or a system from which documents are bulk-indexed.
 * {@link #content} should be plaintext that can easily be indexed and searched in.
 * 
 * Can also be used as a {@link SearchResultItem}.
 * 
 * @author Philipp Gayret
 *
 */
public class BasicDocument implements SearchResultItem {

	private String id;
	private String source;
	private String content;
	private String title;
	private String url;
	private Long timestamp;
	@JsonIgnore
	private String contentHash;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getContentHash() {
		return this.contentHash;
	}

	public void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}

}
