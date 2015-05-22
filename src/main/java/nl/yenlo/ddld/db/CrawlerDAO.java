package nl.yenlo.ddld.db;

import nl.yenlo.ddld.model.Crawler;
import nl.yenlo.ddld.model.User;

import java.util.Collection;

/**
 * @author Philipp Gayret
 */
public interface CrawlerDAO {

    public Crawler get(User owner, Integer id) throws NoMatchException;

    public Crawler get(Integer id) throws NoMatchException;

    public Collection<Crawler> get(User owner);

    public void save(Crawler crawler);

    public void delete(Crawler crawler);

    public Collection<Crawler> getScheduled();

}
