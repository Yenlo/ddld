package nl.yenlo.ddld.model.search;

import nl.yenlo.ddld.model.Factcheck;
import nl.yenlo.ddld.model.User;

/**
 * {@link #getSource()} references a {@link User#getId()}.
 * 
 * @author Philipp Gayret
 *
 */
public class RequestDocument extends BasicDocument {

	/**
	 * references a {@link Factcheck}.
	 * 
	 * for ElasticSearch query purposes, this name would be converted to `db_factcheck_id`.
	 */
	private Integer dbFactcheckId;

	private Boolean highlighted;

	private String parentId;

	public Boolean getHighlighted() {
		return this.highlighted;
	}

	public void setHighlighted(Boolean highlighted) {
		this.highlighted = highlighted;
	}

	public Integer getDbFactcheckId() {
		return this.dbFactcheckId;
	}

	public void setDbFactcheckId(Integer dbFactcheckId) {
		this.dbFactcheckId = dbFactcheckId;
	}

	public String getParentId() {
		return this.parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

}
