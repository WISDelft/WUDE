/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import nl.wisdelft.twitter.gatherer.entity.TwitterList;
import nl.wisdelft.twitter.gatherer.entity.TwitterStatus;
import nl.wisdelft.twitter.gatherer.entity.TwitterUser;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.json.DataObjectFactory;

/**
 * @author oosterman
 */
public class HibernateUtil {
	/**
	 * Singleton sessionfactory, created on request
	 */
	private static SessionFactory sessionFactory;

	protected static void rebuildSessionFactory() {
		sessionFactory = null;
	}

	private static SessionFactory buildSessionFactory() {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			Configuration configuration = new Configuration();
			configuration.configure("hibernate.cfg.xml");
			ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder().applySettings(configuration.getProperties());
			ServiceRegistry registry = serviceRegistryBuilder.buildServiceRegistry();
			return configuration.buildSessionFactory(registry);
		}
		catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			sessionFactory = buildSessionFactory();
		}
		return sessionFactory;
	}

	/**
	 * Creates a TwitterUser from a Twitter4J User. Harvest date is current date.
	 * 
	 * @param user
	 * @return
	 */
	public static TwitterUser toTwitterUser(User user) {
		return toTwitterUser(user, getCurrentDate());
	}

	/**
	 * Creates a TwitterUser from a Twitter4J User with the specified harvest
	 * date.
	 * 
	 * @param user
	 * @param dateHarvested
	 * @return
	 */
	public static TwitterUser toTwitterUser(User user, Date dateHarvested) {
		TwitterUser u = new TwitterUser(user.getId());
		u.setDateHarvested(dateHarvested);
		u.setCreatedAt(user.getCreatedAt());
		u.setDescription(user.getDescription());
		u.setFollowerCount(user.getFollowersCount());
		u.setFriendsCount(user.getFriendsCount());
		u.setLang(user.getLang());
		u.setLocation(user.getLocation());
		u.setName(user.getName());
		u.setScreenName(user.getScreenName());
		u.setURL(user.getURL());
		u.setRawJSON(DataObjectFactory.getRawJSON(user));
		u.setProtectedAccount(user.isProtected());
		return u;
	}

	/**
	 * Bulk create TwitterUsers from Twitter4J Users. Harvest date is current
	 * date.
	 * 
	 * @param users
	 * @return
	 */
	public static Set<TwitterUser> toTwitterUser(Iterable<User> users) {
		return toTwitterUser(users, getCurrentDate());
	}

	/**
	 * Bulk create TwitterUsers from Twitter4J Users with the provided harvest
	 * date
	 * 
	 * @param users
	 * @param dateHarvest
	 * @return
	 */
	public static Set<TwitterUser> toTwitterUser(Iterable<User> users, Date dateHarvest) {
		Set<TwitterUser> s = new HashSet<TwitterUser>();
		for (User user : users) {
			s.add(toTwitterUser(user, dateHarvest));
		}
		return s;
	}

	public static TwitterList toTwitterList(UserList list) {
		TwitterUser u = toTwitterUser(list.getUser());
		TwitterList l = new TwitterList(new Long(list.getId()), u);
		l.setDescription(list.getDescription());
		l.setFullName(list.getFullName());
		l.setMemberCount(list.getMemberCount());
		l.setName(list.getName());
		l.setSlug(list.getSlug());
		l.setSubscriberCount(list.getSubscriberCount());
		l.setUri(list.getURI());

		return l;
	}

	public static Set<TwitterStatus> toTwitterStatus(Iterable<Status> statusses) {
		Set<TwitterStatus> s = new HashSet<TwitterStatus>();
		for (Status status : statusses) {
			s.add(toTwitterStatus(status));
		}
		return s;
	}

	public static TwitterStatus toTwitterStatus(Status status) {
		TwitterStatus s = new TwitterStatus(status.getId(), status.getUser().getId());
		s.setCreatedAt(status.getCreatedAt());
		s.setInReplyToScreenName(status.getInReplyToScreenName());
		s.setInReplyToStatusId(status.getInReplyToStatusId());
		s.setInReplyToUserId(status.getInReplyToUserId());
		s.setIsoLanguageCode(status.getIsoLanguageCode());
		if (status.getGeoLocation() != null) {
			s.setLatitude(status.getGeoLocation().getLatitude());
			s.setLongitude(status.getGeoLocation().getLongitude());
		}
		s.setRawJSON(DataObjectFactory.getRawJSON(status));
		s.setRetweetCount(status.getRetweetCount());
		s.setText(status.getText());

		return s;
	}

	/**
	 * returns a date object containing current year, month and day. Other values
	 * are cleared.
	 * 
	 * @return
	 */
	public static Date getCurrentDate() {
		Calendar c = new GregorianCalendar();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		c.clear();
		c.set(year, month, day);
		return c.getTime();
	}
}
