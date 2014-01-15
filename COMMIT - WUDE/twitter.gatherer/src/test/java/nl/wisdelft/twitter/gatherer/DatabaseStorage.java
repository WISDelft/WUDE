/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author oosterman
 */
public class DatabaseStorage {

	static SessionFactory sessionFactory = null;

	@BeforeClass
	public static void setupOnce() {
		sessionFactory = HibernateUtil.getSessionFactory();
	}

	@AfterClass
	public static void tearDownOnce() {
		if (sessionFactory != null) sessionFactory.close();
	}

	@Test
	public void testCredentials() {
		// Tests wether the provided credentials are correct
		sessionFactory.getCurrentSession().beginTransaction();
	}


	@Test
	public void simpleQuery() {
		// only works for Postgres DB, shows
		Session session = sessionFactory.getCurrentSession();
		Query q = session.createSQLQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';");
		Assert.assertTrue(q.list() != null);
	}

}
