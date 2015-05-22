package nl.yenlo.ddld.db.impl;

import nl.yenlo.ddld.db.NoMatchException;
import nl.yenlo.ddld.db.SourceDAO;
import nl.yenlo.ddld.model.Source;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * @author Philipp Gayret
 */
public class SourceDAOImpl implements SourceDAO {

	@Override
	public void save(Source source) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.saveOrUpdate(source);
		transaction.commit();
		session.disconnect();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Source get(Integer id) throws NoMatchException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<Source> checks = session.createCriteria(Source.class).add(Restrictions.eq("id", id)).list();
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
	public Source get(String name) throws NoMatchException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<Source> checks = session.createCriteria(Source.class).add(Restrictions.eq("name", name)).list();
		transaction.commit();
		session.disconnect();
		try {
			return checks.iterator().next();
		} catch (NoSuchElementException e) {
			throw new NoMatchException();
		}
	}

	@Override
	public boolean exists(String name) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Source item = (Source) session.createCriteria(Source.class).add(Restrictions.eq("name", name)).uniqueResult();
		transaction.commit();
		session.disconnect();
		return item != null;
	}

}
