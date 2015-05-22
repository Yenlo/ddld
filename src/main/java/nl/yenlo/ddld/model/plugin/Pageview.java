package nl.yenlo.ddld.model.plugin;

import nl.yenlo.ddld.model.Factcheck;
import nl.yenlo.ddld.model.User;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Allows for tracking of pageviews, stored in ElasticSearch.
 * 
 * - {@link #userId} references a {@link User}.
 * - {@link #factcheckId} references a {@link Factcheck}.
 * 
 * @author Philipp Gayret
 *
 */
public class Pageview {

	@JsonIgnore
	private Integer userId;
	@JsonIgnore
	private Integer factcheckId;
	private String id;
	private Long targetTimestamp;
	private String targetUrl;
	private Long sourceTimestamp;
	private String sourceUrl;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getFactcheckId() {
		return this.factcheckId;
	}

	public void setFactcheckId(Integer factcheckId) {
		this.factcheckId = factcheckId;
	}

	public Long getTargetTimestamp() {
		return this.targetTimestamp;
	}

	public void setTargetTimestamp(Long targetTimestamp) {
		this.targetTimestamp = targetTimestamp;
	}

	public String getTargetUrl() {
		return this.targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public Long getSourceTimestamp() {
		return this.sourceTimestamp;
	}

	public void setSourceTimestamp(Long sourceTimestamp) {
		this.sourceTimestamp = sourceTimestamp;
	}

	public String getSourceUrl() {
		return this.sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

}
