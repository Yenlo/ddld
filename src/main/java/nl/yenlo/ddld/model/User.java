package nl.yenlo.ddld.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import nl.yenlo.ddld.api.exception.FunctionalException;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * 
 * @author Philipp Gayret
 * 
 */
@Entity
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column
	@JsonIgnore
	private Integer id;
	@Column(unique = true)
	private String email;
	@Column
	@JsonIgnore
	private String password;
	@Column
	@JsonIgnore
	private String salt;
	@OneToOne
	@JsonIgnore
	private Factcheck activeFactcheck;

	// TODO(V2): add additional attributes
	// role: enum [ admin, user ]
	// adres
	// postcode
	// woonplaats
	// telefoonnummer
	// bedrijf

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	/**
	 * Throws an error if there is no active factcheck.
	 * @return
	 */
	@JsonIgnore
	public Factcheck getActiveFactcheckOrErr() {
		if (this.activeFactcheck == null) {
			throw new FunctionalException(FunctionalException.Kind.NO_ACTIVE_FACTCHECKS);
		}
		return this.activeFactcheck;
	}

	public Factcheck getActiveFactcheck() {
		return this.activeFactcheck;
	}

	public void setActiveFactcheck(Factcheck activeFactcheck) {
		this.activeFactcheck = activeFactcheck;
	}

}
