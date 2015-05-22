package nl.yenlo.ddld.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A source of searchable data.
 * 
 * @author Philipp Gayret
 * 
 */
@Entity
public class Source implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column
	@JsonIgnore
	private Integer id;
	@Column(unique = true)
	private String name;
	/**
	 * This should be used when importing documents from the source to authenticate.
	 */
	@Column
	@JsonIgnore
	private String secret;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSecret() {
		return this.secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

}
