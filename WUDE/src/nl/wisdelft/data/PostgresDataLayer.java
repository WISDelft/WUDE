package nl.wisdelft.data;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

public class PostgresDataLayer {
	/* LOCAL */
	/*
	 * static final String dbUsername = "twitter"; static final String dbPassword
	 * = "twitter"; static final String dbName = "twitter"; static final String
	 * dbHost = "localhost";
	 */
	/* PRODUCTION */
	static final String dbUsername = "wisuser";
	static final String dbPassword = "admin@wis";
	static final String dbName = "wis";
	static final String dbHost = "mcheck.st.ewi.tudelft.nl";

	static final String dbType = "postgresql";
	static final String dbDriver = "org.postgresql.Driver";
	static final int dbPort = 5432;

	static final String[] twitterConsumerKey = { "Y2jHCbH4s7QgsrsGzDuw", "pcCUKbT1HHre6bn5nPEcw", "qw0AOxtAdrfVBw0X27Fw",
			"xsEAaxj0nLNMXEw4wnGKw", "qjfzUUp110akJ5Xtt6AZQ" };

	static final String[] twitterConsumerSecret = { "RcQ7tFgFCMULEa5FVse8q3sH9lwx3D6jhrxyAgyCo",
			"B02c9ATQBv3xn7MK7tcKCB0XXcNxvtohOtInQr3BM4", "m6i83j0OKZWb1VFIrzDWbHYTD6ZzRGwJi6oJjNU3so",
			"6JkKcIw6K1M06uUD95TjUUydA8b9Sek0HngA5xoI", "DpSF7YGyTq8P5BxuOXEuPtI23yF1nObZVVX3cMpo5Q" };

	static final String[] twitterOAuthAccessToken = { "158663891-nRM3xCk5k4kIJcn9uzWUPS0DepcmmUDRsAifOR8j",
			"988515800-Pelb8XIgmP6VnTZvMBFvwSgm0M8wJlsa81XZwcK9", "988527696-JVNxL4nrCAdDW8YxdH7ro9JRiAiG9I9Vws4sHgE8",
			"990692244-4F7Qe72BuIjhYWuswXo94QOAaIQ7tyQB9IgpfDSs", "991401782-AhyleWGCBu5CWi1JsrVCLDSyjAvoQszmqTWrDfLN" };

	static final String[] twitterOAuthAccessTokenSecret = { "s40CzQwt101C4mUWMIr2VjznoDv9MRrFu9rDtsGCH38",
			"Z0cpYxjngrpTAmOfqB0iLkoJihBF0OMspFUA3BRznc", "AwvFum0QlTMUagsMqK7w2cDNDOQrSQVIGoQj6enQQI",
			"dnkKbd4GqhdpV6wYCzp2JBLFff3yItxPQDQFbIxLY", "jgC6jQ07z4icLZuEsmKsrDcBVL5rvSSU2R1ajeeeQ" };

	private static Configuration[] configuration = null;
	private Connection connection;
	private PreparedStatement insertTweet, insertTaskTweet, insertUser, insertTask, selectTasks, updateUserTimelineParsed,
			selectTimelineTasks, updateTaskQueried, selectFollowers, selectTweets, updateTweet, selectUnparsedTweets, updateParsedTweet,
			insertTweetUrl, selectUserWithTimeline;

	public PostgresDataLayer() {
		connect();
		try {
			insertTweet = getDBConnection().prepareStatement("INSERT INTO tweet (id,userid,date,json) VALUES (?,?,?,?)");
			insertTaskTweet = getDBConnection().prepareStatement("INSERT INTO tasktweet (taskid,tweetid) VALUES (?,?)");
			insertUser = getDBConnection().prepareStatement("INSERT INTO \"user\" (id) VALUES (?) ");
			insertTask = getDBConnection().prepareStatement(
					"INSERT INTO task (searchstring,latitude,longitude,radius,language,maxtweets,issearch,isstream,addfoundusers) VALUES (?,?,?,?,?,?,?,?,?)");
			selectTasks = getDBConnection().prepareStatement("SELECT * FROM task");
			updateUserTimelineParsed = getDBConnection().prepareStatement("UPDATE \"user\" SET timelineparsed=? WHERE id=?");
			selectTimelineTasks = getDBConnection().prepareStatement(
					"SELECT following.taskid,following.userid FROM following LEFT JOIN \"user\" ON following.userid=\"user\".id WHERE NOT timelineparsed");
			updateTaskQueried = getDBConnection().prepareStatement("UPDATE task SET queried=? WHERE id=?");
			selectFollowers = getDBConnection().prepareStatement("SELECT * FROM following");
			selectTweets = getDBConnection().prepareStatement("SELECT id, content FROM tweet WHERE userid=? AND content IS NOT NULL");
			updateTweet = getDBConnection().prepareStatement("UPDATE tweet SET json=? WHERE id=?");
			selectUnparsedTweets = getDBConnection().prepareStatement("SELECT json FROM tweet WHERE content IS NULL LIMIT ? OFFSET ?");
			updateParsedTweet = getDBConnection().prepareStatement("UPDATE tweet SET content=?, geocoords=? WHERE id=?");
			insertTweetUrl = getDBConnection().prepareStatement("INSERT INTO tweeturl (tweetid, originalurl,expandedurl) VALUES (?,?,?)");
			selectUserWithTimeline = getDBConnection().prepareStatement("SELECT id FROM \"user\" WHERE timelineparsed");
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public Map<Long, List<Integer>> getFollowing() {
		Map<Long, List<Integer>> following = new HashMap<Long, List<Integer>>();
		try {
			ResultSet result = selectFollowers.executeQuery();
			while (result.next()) {
				int taskId = result.getInt("taskid");
				long userId = result.getLong("userid");
				// make sure bucket exists
				if (!following.containsKey(userId)) {
					following.put(userId, new ArrayList<Integer>());
				}
				// add the task to the user
				following.get(userId).add(taskId);
			}
			return following;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return new HashMap<Long, List<Integer>>();
		}
	}

	public Set<JSONStatus> toJSONStatus(Iterable<Status> statusus) {
		Set<JSONStatus> tweets = new HashSet<JSONStatus>();
		for (Status s : statusus) {
			tweets.add(toJSONStatus(s));
		}
		return tweets;
	}

	public JSONStatus toJSONStatus(Status status) {
		String json = DataObjectFactory.getRawJSON(status);
		return new JSONStatus(status, json);
	}

	public List<TwitterParserTask> getTasks() {
		try {
			ResultSet result = selectTasks.executeQuery();
			List<TwitterParserTask> tasks = new ArrayList<TwitterParserTask>();
			TwitterParserTask t;
			while (result.next()) {
				t = new TwitterParserTask();
				t.setId(result.getInt("id"));
				t.setLanguage(result.getString("language"));
				t.setLatitude(result.getFloat("latitude"));
				t.setLongitude(result.getFloat("longitude"));
				t.setMaxTweets(result.getInt("maxtweets"));
				t.setRadius(result.getInt("radius"));
				t.setSearch(result.getBoolean("issearch"));
				t.setStream(result.getBoolean("isstream"));
				t.setSearchString(result.getString("searchstring"));
				t.setAddFoundUsers(result.getBoolean("addfoundusers"));
				t.setActive(result.getBoolean("active"));
				t.setQueried(result.getBoolean("queried"));
				tasks.add(t);
			}
			return tasks;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<TwitterParserTask>();
		}
	}

	public List<TimelineParserTask> getTimelineTasks() {
		try {
			ResultSet result = selectTimelineTasks.executeQuery();
			List<TimelineParserTask> tasks = new ArrayList<TimelineParserTask>();
			TimelineParserTask task;
			while (result.next()) {
				int taskId = result.getInt("taskid");
				long userId = result.getLong("userid");
				task = new TimelineParserTask(userId, taskId);
				tasks.add(task);
			}
			return tasks;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<TimelineParserTask>();
		}
	}

	public Twitter getTwitter() {
		return getTwitter(0);
	}

	public Twitter getTwitter(int index) {
		Configuration config = getTwitterConfiguration(index);
		if (config == null) return null;
		else return new TwitterFactory(config).getInstance();
	}

	public TwitterStream getTwitterStream(int index) {
		Configuration config = getTwitterConfiguration(index);
		if (config == null) return null;
		else return new TwitterStreamFactory(config).getInstance();
	}

	public Set<Status> getUnparsedTweets(int count, int offset) {
		Set<Status> tweets = new HashSet<Status>();
		try {
			selectUnparsedTweets.setInt(1, count);
			selectUnparsedTweets.setInt(2, offset);
			ResultSet result = selectUnparsedTweets.executeQuery();
			String json;
			while (result.next()) {
				json = result.getString("json");
				if (json != null) {
					tweets.add(DataObjectFactory.createStatus(json));
				}
			}
			return tweets;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (TwitterException e) {
			e.printStackTrace();
		}
		// something went wrong. Return an empty list
		return new HashSet<Status>();

	}

	public Set<Long> getUsersWithParsedTimeline() {
		Set<Long> ids = new HashSet<Long>();
		ResultSet result;
		try {
			result = selectUserWithTimeline.executeQuery();
			while (result.next()) {
				ids.add(result.getLong("id"));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return new HashSet<Long>();
		}
		return ids;
	}

	/**
	 * Retrieves all the tweets from the user from the DB, and returns the plain
	 * content
	 * 
	 * @param userId
	 * @return A Map with tweet id, tweet content pairs
	 */
	public Map<Long, String> getUserTweets(Long userId) {
		Map<Long, String> tweets = new HashMap<Long, String>();
		// get all the tweet from the user
		try {
			selectTweets.setLong(1, userId);
			ResultSet result = selectTweets.executeQuery();
			while (result.next()) {
				String content = result.getString("content");
				Long id = result.getLong("id");
				tweets.put(id, content);
			}
			return tweets;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return new HashMap<Long, String>();
		}
	}

	public void setSearchTaskQueried(int taskId, boolean queried) {
		try {
			updateTaskQueried.setBoolean(1, queried);
			updateTaskQueried.setInt(2, taskId);
			updateTaskQueried.executeUpdate();
			commit();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			rollback();
		}
	}

	public void setUserTimelineParsed(long userId, boolean parsed) {
		try {
			updateUserTimelineParsed.setBoolean(1, parsed);
			updateUserTimelineParsed.setLong(2, userId);
			updateUserTimelineParsed.executeUpdate();
			commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
			rollback();
		}

	}

	public void storeFollowing(Set<Long> userIDs, TwitterParserTask task) {
		if (userIDs == null || userIDs.size() == 0) return;
		try {
			// create the users if they do not already exist
			StringBuilder sb = new StringBuilder();
			for (Long id : userIDs) {
				if (sb.length() > 0) sb.append(",");
				sb.append(id);
			}
			sb.insert(0, "SELECT id FROM \"user\" WHERE id IN (");
			sb.append(")");
			ResultSet set = getDBConnection().createStatement().executeQuery(sb.toString());
			Set<Long> existingIds = new HashSet<Long>();
			// put the ids that already exist in a set
			while (set.next()) {
				existingIds.add(set.getLong(1));
			}
			// batch insert of the new users
			insertUser.clearBatch();
			for (Long id : userIDs) {
				if (!existingIds.contains(id)) {
					insertUser.setLong(1, id);
					insertUser.addBatch();
				}
			}
			insertUser.executeBatch();

			// add the followers
			sb = new StringBuilder();
			for (Long id : userIDs) {
				if (sb.length() > 0) sb.append(",");
				sb.append(id);
			}
			sb.insert(0, String.format("SELECT userid FROM following WHERE taskid=%s AND userid IN (", task.getId()));
			sb.append(")");
			set = getDBConnection().createStatement().executeQuery(sb.toString());
			existingIds = new HashSet<Long>();
			// put the ids that already exist in a set
			while (set.next()) {
				existingIds.add(set.getLong(1));
			}
			List<Long> toInsert = new ArrayList<Long>(userIDs.size());
			for (Long id : userIDs) {
				if (!existingIds.contains(id)) toInsert.add(id);
			}

			// create the insert sql statement
			sb = new StringBuilder();

			for (Long id : toInsert) {
				if (sb.length() > 0) sb.append(",");
				sb.append("(");
				sb.append(task.getId());
				sb.append(",");
				sb.append(id);
				sb.append(")");
			}
			sb.insert(0, "INSERT INTO following (taskid,userid) VALUES ");
			// add the userIDs to the list and to the DB
			getDBConnection().createStatement().executeUpdate(sb.toString());

			// commit everything
			commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
			rollback();
		}
	}

	/**
	 * Stores the task in the DB
	 * 
	 * @param t
	 * @return Task {@value t} with the generated DB ID
	 */
	public TwitterParserTask storeTask(TwitterParserTask t) {
		try {
			// insert task
			insertTask.setString(1, t.getSearchString());
			if (t.getLatitude() == null) insertTask.setNull(2, Types.DOUBLE);
			else insertTask.setDouble(2, t.getLatitude());
			if (t.getLongitude() == null) insertTask.setNull(3, Types.DOUBLE);
			else insertTask.setDouble(3, t.getLongitude());
			if (t.getRadius() == null) insertTask.setNull(4, Types.INTEGER);
			else insertTask.setInt(4, t.getRadius());
			insertTask.setString(5, t.getLanguage());
			if (t.getMaxTweets() == null) insertTask.setNull(6, Types.INTEGER);
			else insertTask.setInt(6, t.getMaxTweets());
			insertTask.setBoolean(7, t.isSearch());
			insertTask.setBoolean(8, t.isStream());
			insertTask.setBoolean(9, t.isAddFoundUsers());
			insertTask.executeUpdate();
			// get id
			ResultSet set = getDBConnection().createStatement().executeQuery("SELECT CURRVAL('taskidseq')");
			set.next();
			t.setId(set.getInt(1));

			commit();
			return t;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			rollback();
			return null;
		}
	}

	/**
	 * Stores all tweets and tasktweets in batches and in one transaction
	 * 
	 * @param tweets
	 * @param taskId
	 * @return Whether the tweets were stores succesfully
	 */
	public boolean storeTweets(Set<JSONStatus> tweets, int taskId) {
		if (tweets == null || tweets.size() == 0) return true;
		StringBuilder sb = new StringBuilder();
		try {
			// clear the batches
			insertTweet.clearBatch();
			insertTaskTweet.clearBatch();

			// First insert all tweets that do not exist yet
			for (JSONStatus tweet : tweets) {
				if (sb.length() > 0) sb.append(",");
				sb.append(tweet.getId());
			}
			sb.insert(0, "SELECT id FROM tweet WHERE id IN (");
			sb.append(")");
			ResultSet set = getDBConnection().createStatement().executeQuery(sb.toString());
			Set<Long> existingIds = new HashSet<Long>();
			// put the tweetids that already exist in a set
			while (set.next()) {
				existingIds.add(set.getLong(1));
			}
			// now only insert the tweets not already existing
			for (JSONStatus tweet : tweets) {
				if (!existingIds.contains(tweet.getId())) {
					insertTweet.setLong(1, tweet.getId());
					insertTweet.setLong(2, tweet.getUser().getId());
					insertTweet.setTimestamp(3, new Timestamp(tweet.getCreatedAt().getTime()));
					insertTweet.setString(4, tweet.getJSON());
					insertTweet.addBatch();
				}
			}
			// execute batch
			insertTweet.executeBatch();

			sb = new StringBuilder();
			// Add the tasktweets that not already exist
			for (JSONStatus tweet : tweets) {
				if (sb.length() > 0) sb.append(",");
				sb.append(tweet.getId());
			}
			sb.insert(0, String.format("SELECT tweetid FROM tasktweet WHERE taskid=%s AND tweetid IN (", taskId));
			sb.append(")");
			set = getDBConnection().createStatement().executeQuery(sb.toString());
			existingIds = new HashSet<Long>();
			while (set.next()) {
				existingIds.add(set.getLong(1));
			}
			// now only insert the task tweet links not already existing
			for (JSONStatus tweet : tweets) {
				if (!existingIds.contains(tweet.getId())) {
					insertTaskTweet.setInt(1, taskId);
					insertTaskTweet.setLong(2, tweet.getId());
					insertTaskTweet.addBatch();
				}
			}

			// execute batch
			insertTaskTweet.executeBatch();

			// commit everything
			commit();
			return true;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			rollback();
			return false;
		}
	}

	/**
	 * Update the tweets and inserts the tweeturl in one transaction
	 * 
	 * @param parsedTweets
	 */
	public void updateParsedTweets(Iterable<ParsedTweet> parsedTweets) {
		try {
			updateParsedTweet.clearBatch();
			insertTweetUrl.clearBatch();
			// put all in a batch
			for (ParsedTweet ptweet : parsedTweets) {
				// update the tweet
				updateParsedTweet.setString(1, ptweet.getContent());
				updateParsedTweet.setString(2, ptweet.getGeocoords());
				updateParsedTweet.setLong(3, ptweet.getTweetId());
				updateParsedTweet.addBatch();

				// insert the urls
				for (ParsedUrl url : ptweet.getUrls()) {
					insertTweetUrl.setLong(1, ptweet.getTweetId());
					insertTweetUrl.setString(2, url.getOriginalurl());
					insertTweetUrl.setString(3, url.getExpandedurl());
					insertTweetUrl.addBatch();
				}
			}
			// submit the batch
			updateParsedTweet.executeBatch();
			insertTweetUrl.executeBatch();
			// commit
			commit();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			rollback();
		}

	}

	public void updateTweet(JSONStatus tweet) {
		try {
			updateTweet.setString(1, tweet.getJSON());
			updateTweet.setLong(2, tweet.getId());
			updateTweet.executeUpdate();
			commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
			rollback();
		}
	}

	private void commit() throws SQLException {
		getDBConnection().commit();
	}

	private void connect() {
		try {
			Class.forName(dbDriver);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			connection = DriverManager.getConnection(String.format("jdbc:%s://%s:%s/%s", dbType, dbHost, dbPort, dbName), dbUsername, dbPassword);
			connection.setAutoCommit(false);
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private Connection getDBConnection() {
		if (connection == null) connect();
		if (!isValid()) return null;
		else return connection;
	}

	/**
	 * @param applicationConfiguration Number starting with 0 indicating the
	 *          number of the configuration
	 * @return
	 */
	private Configuration getTwitterConfiguration(int index) {
		// check if there are configs
		if (configuration == null) {
			configuration = new Configuration[twitterConsumerKey.length];
			for (int i = 0; i < twitterConsumerKey.length; i++)
				configuration[i] = null;
		}

		// check for the right number
		if (index < 0 || index >= configuration.length) return null;

		// check if we have the configuration
		if (configuration[index] == null) {
			// this config has not been created yet
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setJSONStoreEnabled(true).setOAuthConsumerKey(twitterConsumerKey[index]).setOAuthConsumerSecret(
					twitterConsumerSecret[index]).setOAuthAccessToken(twitterOAuthAccessToken[index]).setOAuthAccessTokenSecret(
					twitterOAuthAccessTokenSecret[index]);
			configuration[index] = cb.build();
		}
		return configuration[index];
	}

	private boolean isValid() {
		try {
			return connection.isValid(300);
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void rollback() {
		try {
			connection.rollback();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			connect();
		}
	}

}
