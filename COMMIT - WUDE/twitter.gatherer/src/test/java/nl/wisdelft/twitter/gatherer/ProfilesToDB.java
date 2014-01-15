/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import junit.framework.Assert;
import nl.wisdelft.twitter.gatherer.configuration.AppConfiguration;
import nl.wisdelft.twitter.gatherer.entity.TwitterUser;
import nl.wisdelft.twitter.gatherer.repository.BaseRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import twitter4j.TwitterException;

/**
 * @author oosterman
 */
public class ProfilesToDB {
	AppConfiguration appConfig;
	SessionFactory sessionFactory;
	Date currentDate;
	Session session;

	@After
	public void teardown() {
		if (!sessionFactory.isClosed()) sessionFactory.close();
	}

	@Before
	public void setup() {
		HibernateUtil.rebuildSessionFactory();
		sessionFactory = HibernateUtil.getSessionFactory();
		session = sessionFactory.getCurrentSession();

		// create appconfig
		appConfig = new AppConfiguration();
		appConfig.getProfile = true;
		appConfig.toDB = true;

		// date
		currentDate = HibernateUtil.getCurrentDate();
	}

	@Test
	public void noAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] {};
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterUser> repo = new BaseRepository<TwitterUser>(TwitterUser.class);
		int size = repo.findAll().size();
		t.rollback();
		Assert.assertTrue(size == 0);
	}

	@Test
	public void publicAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.publicAccount };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterUser> repo = new BaseRepository<TwitterUser>(TwitterUser.class);
		Set<TwitterUser> users = repo.findAll();
		Assert.assertTrue(users.size() == 1);
		t.rollback();
	}

	@Test
	public void noFollowers() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.noFollowers };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterUser> repo = new BaseRepository<TwitterUser>(TwitterUser.class);
		Set<TwitterUser> users = repo.findAll();
		Assert.assertTrue(users.size() == 1);
		t.rollback();
	}

	@Test
	public void multipleAccounts() throws IOException, TwitterException {
		appConfig.ids = TestConstants.multipleAccounts;
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterUser> repo = new BaseRepository<TwitterUser>(TwitterUser.class);
		Set<TwitterUser> users = repo.findAll();
		Assert.assertTrue(users.size() == TestConstants.multipleAccounts.length);
		t.rollback();
	}

	@Test
	public void privateAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.privateAccount };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterUser> repo = new BaseRepository<TwitterUser>(TwitterUser.class);
		Set<TwitterUser> users = repo.findAll();
		Assert.assertTrue(users.size() == 1);
		t.rollback();
	}

}
