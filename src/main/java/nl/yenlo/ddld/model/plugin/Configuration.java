package nl.yenlo.ddld.model.plugin;

/**
 * An object defining a User's plugin configuration.
 * 
 * @author Philipp Gayret
 *
 */
public class Configuration {

	private Long indexingTimeout;

	public Long getIndexingTimeout() {
		return this.indexingTimeout;
	}

	public void setIndexingTimeout(Long indexingTimeout) {
		this.indexingTimeout = indexingTimeout;
	}

}
