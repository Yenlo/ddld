package nl.yenlo.ddld.db.impl;

import nl.yenlo.ddld.db.FactcheckDAO;
import nl.yenlo.ddld.db.NoMatchException;
import nl.yenlo.ddld.model.Factcheck;
import nl.yenlo.ddld.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * @author Philipp Gayret
 */
public class FactcheckDAOImpl implements FactcheckDAO {

	@Override
	@SuppressWarnings("unchecked")
	public Factcheck get(User owner, Integer id) throws NoMatchException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<Factcheck> checks = session.createCriteria(Factcheck.class).add(Restrictions.eq("owner", owner)).add(Restrictions.eq("id", id)).list();
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
	public Collection<Factcheck> get(User owner) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<Factcheck> checks = session.createCriteria(Factcheck.class).add(Restrictions.eq("owner", owner)).list();
		transaction.commit();
		session.disconnect();
		return checks;
	}

	@Override
	public void save(Factcheck factcheck) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.saveOrUpdate(factcheck);
		transaction.commit();
		session.disconnect();
	}

	@Override
	public void delete(Factcheck factcheck) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(factcheck);
		transaction.commit();
		session.disconnect();
	}

}
