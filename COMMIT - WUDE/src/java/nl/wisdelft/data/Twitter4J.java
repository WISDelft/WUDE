/**
 * 
 */
package nl.wisdelft.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.wisdelft.WUDEUtil;
import nl.wisdelft.WUDEUtil.Property;
import nl.wisdelft.data.entity.TwitterList;
import nl.wisdelft.data.entity.TwitterStatus;
import nl.wisdelft.data.entity.TwitterUser;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * @author oosterman
 */
public class Twitter4J {
	private List<Twitter> twitterInstances;
	private List<TwitterStream> twitterStreamInstances;
	private static Twitter4J twitter4j;
	private static String configFile = WUDEUtil.getProperty(Property.TWITTERCONFIGFILE);

	private Twitter4J(String configFile) throws IOException, TwitterException {
		File file = new File(configFile);
		if (!file.isFile()) throw new FileNotFoundException("Configuration file '" + configFile + "' does not exist");
		loadConfiguration(file);
	}

	public static synchronized Twitter4J getInstance() throws IOException, TwitterException {
		if (!configFile.equals(Twitter4J.configFile) || twitter4j == null) {
			twitter4j = new Twitter4J(configFile);
		}
		return twitter4j;
	}

	/**
	 * Get the twitter instance on index position
	 * 
	 * @param index
	 * @return
	 * @throws TwitterException
	 */
	private Twitter getTwitter(int index) throws TwitterException {
		// check if the instance with this index exists
		if (index >= 0 && index < twitterInstances.size()) {
			return twitterInstances.get(index);
		}
		else {
			throw new TwitterException("Twitterinstance with ID " + index + " does not exist");
		}
	}

	/**
	 * Gets the first twitter instance
	 * 
	 * @return
	 * @throws TwitterException
	 */
	public Twitter getTwitterInstance() throws TwitterException {
		return getTwitterInstance(null);
	}

	/**
	 * Gets the next twitter instance
	 * 
	 * @param twitter instance currently held
	 * @return
	 * @throws TwitterException
	 */
	public Twitter getTwitterInstance(Twitter curTwitter) throws TwitterException {
		if (curTwitter == null) {
			return getTwitter(0);
		}
		else {
			// find the given twitter instance
			int curId = -1;
			for (int i = 0; i < twitterInstances.size(); i++) {
				if (twitterInstances.get(i) == curTwitter) {
					curId = i;
				}
			}
			// if it is not the last one
			if (curId + 1 < twitterInstances.size()) {
				return twitterInstances.get(curId + 1);
			}
			else {
				return twitterInstances.get(0);
			}
		}

	}

	public TwitterStream getTwitterStream(int index) throws TwitterException {
		if (index >= 0 && index < twitterStreamInstances.size()) {
			return twitterStreamInstances.get(index);
		}
		else {
			throw new TwitterException("TwitterStream instance with ID " + index + " not available");
		}
	}

	private void loadConfiguration(File file) throws IOException, TwitterException {
		twitterInstances = new ArrayList<Twitter>();
		twitterStreamInstances = new ArrayList<TwitterStream>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.trim().isEmpty() || line.startsWith("#")) continue;
			String[] a_line = line.split(",");
			TwitterAuthentication auth = new TwitterAuthentication(a_line[0].trim(), a_line[1].trim(), a_line[2].trim(), a_line[3].trim(), a_line[4].trim());
			Configuration config = getTwitterConfiguration(auth);
			Twitter tw = new TwitterFactory(config).getInstance();
			tw.verifyCredentials();
			TwitterStream tws = new TwitterStreamFactory(config).getInstance();
			twitterInstances.add(tw);
			twitterStreamInstances.add(tws);
		}
		reader.close();
		System.out.println("Loaded " + twitterInstances.size() + " twitter authentications.");
	}

	private Configuration getTwitterConfiguration(TwitterAuthentication auth) throws TwitterException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setJSONStoreEnabled(true).setOAuthConsumerKey(auth.consumerKey).setOAuthConsumerSecret(auth.consumerSecret).setOAuthAccessToken(
				auth.accessToken).setOAuthAccessTokenSecret(auth.accessTokenSecret).setPrettyDebugEnabled(true);
		return cb.build();
	}

	public static TwitterUser toTwitterUser(User user) {
		TwitterUser u = new TwitterUser(user.getId());
		u.setCreatedAt(user.getCreatedAt());
		u.setDescription(user.getDescription());
		u.setFollowerCount(user.getFollowersCount());
		u.setFriendsCount(user.getFriendsCount());
		u.setLang(user.getLang());
		u.setLocation(user.getLocation());
		u.setName(user.getName());
		u.setScreenName(user.getScreenName());
		u.setURL(user.getURL());

		return u;
	}

	public static Set<TwitterUser> toTwitterUser(Iterable<User> users) {
		Set<TwitterUser> s = new HashSet<TwitterUser>();
		for (User user : users) {
			s.add(toTwitterUser(user));
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
		TwitterUser u = toTwitterUser(status.getUser());
		TwitterStatus s = new TwitterStatus(status.getId(), u);

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

	class TwitterAuthentication {
		public String name;
		public String consumerKey;
		public String consumerSecret;
		public String accessToken;
		public String accessTokenSecret;

		public TwitterAuthentication(String name, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
			this.name = name;
			this.consumerKey = consumerKey;
			this.consumerSecret = consumerSecret;
			this.accessToken = accessToken;
			this.accessTokenSecret = accessTokenSecret;
		}
	}

}
