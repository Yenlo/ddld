package nl.yenlo.ddld.db;

import nl.yenlo.ddld.model.User;

/**
 * @author Philipp Gayret
 */
public interface UserDAO {

	public void save(User user);

	public User get(Integer id) throws NoMatchException;

	public User get(String email) throws NoMatchException;

	public boolean exists(String email);

}
