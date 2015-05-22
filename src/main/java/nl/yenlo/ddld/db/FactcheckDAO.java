package nl.yenlo.ddld.db;

import nl.yenlo.ddld.model.Factcheck;
import nl.yenlo.ddld.model.User;

import java.util.Collection;

/**
 * @author Philipp Gayret
 */
public interface FactcheckDAO {

    public Factcheck get(User owner, Integer id) throws NoMatchException;

    public Collection<Factcheck> get(User owner);

    public void save(Factcheck factcheck);

    public void delete(Factcheck factcheck);

}
