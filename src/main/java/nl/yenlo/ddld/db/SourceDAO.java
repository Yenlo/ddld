package nl.yenlo.ddld.db;

import nl.yenlo.ddld.model.Source;

/**
 * @author Philipp Gayret
 */
public interface SourceDAO {

	public void save(Source source);

	public Source get(Integer id) throws NoMatchException;

	public Source get(String name) throws NoMatchException;

	public boolean exists(String name);

}
