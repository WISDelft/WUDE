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
import nl.wisdelft.twitter.gatherer.entity.TwitterStatus;
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
public class TweetsToDB {

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
		appConfig.getTweets = true;
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
		BaseRepository<TwitterStatus> repo = new BaseRepository<TwitterStatus>(TwitterStatus.class);
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
		BaseRepository<TwitterStatus> repo = new BaseRepository<TwitterStatus>(TwitterStatus.class);
		Set<TwitterStatus> tweets = repo.findAll();
		t.rollback();
		Assert.assertTrue(tweets.size() > 0);
		for (TwitterStatus tweet : tweets) {
			Assert.assertEquals(tweet.getUserId(), TestConstants.publicAccount);
		}
	}

	@Test
	public void noTweets() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.noTweets };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterStatus> repo = new BaseRepository<TwitterStatus>(TwitterStatus.class);
		int size = repo.findAll().size();
		t.rollback();
		Assert.assertTrue(size == 0);
	}

	@Test
	public void multipleAccounts() throws IOException, TwitterException {
		appConfig.ids = TestConstants.multipleAccounts;
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterStatus> repo = new BaseRepository<TwitterStatus>(TwitterStatus.class);
		Set<TwitterStatus> tweets = repo.findAll();
		t.rollback();
		Set<Long> ids = new HashSet<Long>();
		for (TwitterStatus tweet : tweets) {
			ids.add(tweet.getUserId());
		}
		Assert.assertEquals(TestConstants.multipleAccounts.length, ids.size());
	}

	@Test
	public void privateAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.privateAccount };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterStatus> repo = new BaseRepository<TwitterStatus>(TwitterStatus.class);
		int size = repo.findAll().size();
		t.rollback();
		Assert.assertTrue(size == 0);
	}

	@Test
	public void manyTweets() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.manyTweets };
		App app = new App(appConfig);
		app.startApplication();
		Transaction t = session.beginTransaction();
		BaseRepository<TwitterStatus> repo = new BaseRepository<TwitterStatus>(TwitterStatus.class);
		int size = repo.findAll().size();
		t.rollback();
		Assert.assertTrue(size >= 3200);
	}

}
