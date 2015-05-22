package nl.yenlo.ddld.db.impl;

import nl.yenlo.ddld.db.CrawlerDAO;
import nl.yenlo.ddld.db.NoMatchException;
import nl.yenlo.ddld.model.Crawler;
import nl.yenlo.ddld.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.Calendar;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * @author Philipp Gayret
 */
public class CrawlerDAOImpl implements CrawlerDAO {

	@Override
	@SuppressWarnings("unchecked")
	public Crawler get(User owner, Integer id) throws NoMatchException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<Crawler> checks = session.createCriteria(Crawler.class).add(Restrictions.eq("owner", owner)).add(Restrictions.eq("id", id)).list();
		transaction.commit();
		session.disconnect();
		try {
			return checks.iterator().next();
		} catch (NoSuchElementException e) {
			throw new NoMatchException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Crawler get(Integer id) throws NoMatchException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<Crawler> checks = session.createCriteria(Crawler.class).add(Restrictions.eq("id", id)).list();
		transaction.commit();
		session.disconnect();
		try {
			return checks.iterator().next();
		} catch (NoSuchElementException e) {
			throw new NoMatchException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Crawler> get(User owner) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<Crawler> checks = session.createCriteria(Crawler.class).add(Restrictions.eq("owner", owner)).list();
		transaction.commit();
		session.disconnect();
		return checks;
	}

	@Override
	public void save(Crawler crawler) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.saveOrUpdate(crawler);
		transaction.commit();
		session.disconnect();
	}

	@Override
	public void delete(Crawler crawler) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(crawler);
		transaction.commit();
		session.disconnect();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Crawler> getScheduled() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Long now = Calendar.getInstance().getTimeInMillis();
		Collection<Crawler> checks = session.createCriteria(Crawler.class).add(Restrictions.lt("crawlScheduled", now)).list();
		transaction.commit();
		session.disconnect();
		return checks;
	}

}
