/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author oosterman
 */
public class TwitterConnectionManager {
	private List<TwitterConnection> twitterInstances = new ArrayList<TwitterConnection>();
	private List<TwitterStream> twitterStreamInstances = new ArrayList<TwitterStream>();
	private static TwitterConnectionManager connectionManager = null;
	private static String configFile = "twitter.properties";

	public enum ConnectionMode {
		SINGLE, POOL
	};

	/**
	 * Singleton class
	 * 
	 * @return
	 * @throws IOException
	 * @throws TwitterException
	 */
	public static synchronized TwitterConnectionManager getInstance() throws IOException, TwitterException {
		return getInstance(configFile);
	}

	/**
	 * Singleton class
	 * 
	 * @return
	 * @throws IOException
	 * @throws TwitterException
	 */
	public static synchronized TwitterConnectionManager getInstance(String configFile) throws IOException, TwitterException {
		if (connectionManager == null || !configFile.equals(TwitterConnectionManager.configFile)) {
			connectionManager = new TwitterConnectionManager(configFile);
		}
		return connectionManager;
	}

	private TwitterConnectionManager(String configFile) throws IOException, TwitterException {
		loadConfiguration(configFile);
	}

	/**
	 * Get the twitter instance and return a copy of the TwitterConnection object
	 * 
	 * @param index
	 * @return
	 * @throws TwitterException
	 */
	private TwitterConnection getTwitter(int index) {
		// check if the instance with this index exists
		if (index >= 0 && index < twitterInstances.size()) {
			TwitterConnection con = new TwitterConnection(twitterInstances.get(index).name, twitterInstances.get(index).connection);
			return con;
		}
		else {
			// TODO: Log the access of an illegal index
			return null;
		}
	}

	/**
	 * Gets the first twitter instance
	 * 
	 * @return
	 * @throws TwitterException
	 */
	public TwitterConnection getTwitterInstance() {
		return getTwitterInstance(null);
	}

	/**
	 * Gets the next twitter instance
	 * 
	 * @param twitter instance currently held
	 * @return
	 * @throws TwitterException
	 */
	public TwitterConnection getTwitterInstance(TwitterConnection connection) {
		if (connection == null) {
			return getTwitter(0);
		}
		else {
			// find the given twitter instance
			int curId = -1;
			for (int i = 0; i < twitterInstances.size(); i++) {
				if (twitterInstances.get(i).equals(connection)) {
					curId = i;
					break;
				}
			}
			// if it is not the last one
			if (curId + 1 < twitterInstances.size()) {
				return getTwitter(curId + 1);
			}
			else {
				return getTwitter(0);
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

	private void loadConfiguration(String file) throws IOException, TwitterException {
		// load the configuration from the properties files
		Properties prop = new Properties();
		// resource stream
		InputStream stream = ClassLoader.getSystemResourceAsStream(file);
		if (stream == null) {
			throw new FileNotFoundException("No resource with name '" + file + "' found.");
		}
		prop.load(stream);
		// for each property that starts with "usem" create a new configuration.
		for (String p : prop.stringPropertyNames()) {
			if (p.startsWith("usem")) {
				String val = prop.getProperty(p);
				String[] keys = val.split(",");
				// extra objects for reading clarity and debugging
				// Object containing the authentication info read from the properties
				// file
				TwitterAuthentication auth = new TwitterAuthentication(p, keys[0].trim(), keys[1].trim(), keys[2].trim(), keys[3].trim());
				// Twitter4J configuration object
				Configuration config = getTwitterConfiguration(auth);
				// Twitter connection, based on the configuration
				Twitter connection = new TwitterFactory(config).getInstance();
				// verify the connection
				try {
					connection.verifyCredentials();
				}
				catch (TwitterException ex) {
					System.err.println("Could not verify auth info for '" + p + "'.");
					throw ex;
				}
				// Container object for the connection and its name
				TwitterConnection twitterConnection = new TwitterConnection(p, connection);
				// Twitter stream connection, based on the configuration
				TwitterStream tws = new TwitterStreamFactory(config).getInstance();
				twitterInstances.add(twitterConnection);
				twitterStreamInstances.add(tws);
			}
		}
		System.out.println("Loaded " + twitterInstances.size() + " twitter authentications.");
	}

	private Configuration getTwitterConfiguration(TwitterAuthentication auth) throws TwitterException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		/*
		 * cb.setDebugEnabled(true).setJSONStoreEnabled(true).setOAuthConsumerKey(auth
		 * .
		 * consumerKey).setOAuthConsumerSecret(auth.consumerSecret).setOAuthAccessToken
		 * ( auth.accessToken).setOAuthAccessTokenSecret(auth.accessTokenSecret).
		 * setPrettyDebugEnabled(true);
		 */
		cb.setJSONStoreEnabled(true).setApplicationOnlyAuthEnabled(false).setOAuthConsumerKey(auth.consumerKey).setOAuthConsumerSecret(
				auth.consumerSecret).setPrettyDebugEnabled(true).setDebugEnabled(true).setUseSSL(true).setOAuthAccessToken(auth.accessToken).setOAuthAccessTokenSecret(
				auth.accessTokenSecret);
		return cb.build();
	}

	/**
	 * Determines whether a twitterexception can be seen a success and sleeps the
	 * thread or changes connection when necessary
	 * 
	 * @param ex The exception
	 * @return whether the exception can be seen as a success
	 */
	public synchronized boolean handleTwitterException(TwitterException ex, ConnectionMode connectionMode, TwitterConnection connection) {
		// handle rate limit exceptions
		if (ex.exceededRateLimitation()) {
			int seconds = ex.getRateLimitStatus().getSecondsUntilReset();
			seconds = Math.max(0, seconds);
			System.out.print("Rate limit on " + connection.name + " (TTR " + seconds + " sec).");
			try {
				// In SINGLE mode wait until the rate limit is reset. For POOL when we
				// have to wait less than 30 seconds
				if (connectionMode == ConnectionMode.SINGLE || seconds < 30) {

					System.out.println("We wait " + seconds + " seconds.");
					Thread.sleep(1000 * (seconds + 1));
				}
				else {
					// else change connection and wait
					TwitterConnection newConnection = getTwitterInstance(connection);
					connection.connection = newConnection.connection;
					connection.name = newConnection.name;
					// wait 10 seconds to not spam the connection when all connections
					// are rate limitted.
					System.out.println(" new connection: " + connection.name);
					Thread.sleep(1000 * 10);
				}
			}
			catch (InterruptedException ex2) {
				ex2.printStackTrace();
			}
			// request failed
			return false;
		}
		// the user does not exist or has a private profile
		else if (ex.getStatusCode() == 404 || ex.getStatusCode() == 401) {
			// the best we can get for this request --> request success
			return true;
		}
		// twitter servers under heavy load or network error or twitter internal
		// server error
		else if (ex.getStatusCode() == 503 || ex.isCausedByNetworkIssue() || ex.getStatusCode() == 500) {
			// request failed, but wait a bit before retrying
			try {
				Thread.sleep(1000 * 5);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			return false;
		}
		else {
			// unknown error
			ex.printStackTrace();
			return false;
		}
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
