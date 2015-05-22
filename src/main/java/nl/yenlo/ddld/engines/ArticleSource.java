package nl.yenlo.ddld.engines;

/**
 * An interface for every article engine client to implement.
 * 
 * @author Philipp Gayret
 *
 */
public interface ArticleSource {

	public Article get(String id);

}
