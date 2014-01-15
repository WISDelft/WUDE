/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Assert;
import nl.wisdelft.twitter.gatherer.configuration.AppConfiguration;
import nl.wisdelft.twitter.gatherer.entity.TwitterFollower;
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
public class FollowersToDB {

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
		appConfig.getFollowers = true;
		appConfig.toDB = true;
		appConfig.followersMaxCount = 100;

		// date
		currentDate = HibernateUtil.getCurrentDate();
	}

	@Test
	public void noAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] {};
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterFollower> repo = new BaseRepository<TwitterFollower>(TwitterFollower.class);
		int size = repo.findAll().size();
		t.rollback();
		Assert.assertEquals(size, 0);
	}

	@Test
	public void publicAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.publicAccount };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterFollower> repo = new BaseRepository<TwitterFollower>(TwitterFollower.class);
		Set<TwitterFollower> followers = repo.findAll();
		t.rollback();
		Assert.assertTrue(followers.size() > 0);
		for (TwitterFollower follower : followers) {
			Assert.assertEquals(follower.getUserToId(), TestConstants.publicAccount);
		}
	}

	@Test
	public void noFollowers() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.noFollowers };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterFollower> repo = new BaseRepository<TwitterFollower>(TwitterFollower.class);
		int count = repo.findAll().size();
		t.rollback();
		Assert.assertEquals(count, 0);
	}
	
	@Test
	public void duplicateAccounts() throws IOException, TwitterException{
		appConfig.ids = new long[] { TestConstants.publicAccount, TestConstants.publicAccount, TestConstants.publicAccount };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterFollower> repo = new BaseRepository<TwitterFollower>(TwitterFollower.class);
		int count = repo.findAll().size();
		t.rollback();
		Assert.assertTrue(count > 0);
	}

	@Test
	public void multipleAccounts() throws IOException, TwitterException {
		appConfig.ids = TestConstants.multipleAccounts;
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterFollower> repo = new BaseRepository<TwitterFollower>(TwitterFollower.class);
		Set<TwitterFollower> tws = repo.findAll();
		t.rollback();
		Set<Long> ids = new HashSet<Long>();
		for (TwitterFollower tw : tws) {
			ids.add(tw.getUserToId());
		}
		Assert.assertEquals(TestConstants.multipleAccounts.length, ids.size());

	}

	@Test
	public void privateAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.privateAccount };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterFollower> repo = new BaseRepository<TwitterFollower>(TwitterFollower.class);
		int count = repo.findAll().size();
		t.rollback();
		Assert.assertTrue(count == 0);
	}

	@Test
	public void testOverwrite() {
		Assert.fail();
	}

}
