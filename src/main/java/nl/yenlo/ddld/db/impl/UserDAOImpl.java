package nl.yenlo.ddld.db.impl;

import nl.yenlo.ddld.db.NoMatchException;
import nl.yenlo.ddld.db.UserDAO;
import nl.yenlo.ddld.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * @author Philipp Gayret
 */
public class UserDAOImpl implements UserDAO {

	@Override
	@SuppressWarnings("unchecked")
	public User get(Integer id) throws NoMatchException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<User> checks = session.createCriteria(User.class).add(Restrictions.eq("id", id)).list();
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
	public User get(String email) throws NoMatchException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Collection<User> checks = session.createCriteria(User.class).add(Restrictions.eq("email", email)).list();
		transaction.commit();
		session.disconnect();
		try {
			return checks.iterator().next();
		} catch (NoSuchElementException e) {
			throw new NoMatchException();
		}
	}

	@Override
	public void save(User user) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.saveOrUpdate(user);
		transaction.commit();
		session.disconnect();
	}

	@Override
	public boolean exists(String email) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		User user = (User) session.createCriteria(User.class).add(Restrictions.eq("email", email)).uniqueResult();
		transaction.commit();
		session.disconnect();
		return user != null;
	}

}
