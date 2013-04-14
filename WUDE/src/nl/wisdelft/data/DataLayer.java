package nl.wisdelft.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

public class DataLayer {

	static final int geoPrecisionDecimals = 4;

	static final String twitterCredentialsPath = "/Users/oosterman/Documents/Data/twitterAuthentication.csv";
	private static List<Twitter> twitterInstances = new ArrayList<Twitter>();
	private static List<TwitterStream> twitterStreamInstances = new ArrayList<TwitterStream>();

	private Connection connection;
	private PreparedStatement insertTweet, insertTaskTweet, insertUser, insertTask, selectTasks, updateUserTimelineParsed,
			selectTimelineTasks, updateTaskQueried, selectFollowers, selectTweetsContent, selectTweetsRawJSON, updateTweet, selectUnparsedTweets,
			updateParsedTweet, insertTweetUrl, selectUserWithTimeline, selectGeoFromTweets, selectGeo, insertGeo, removeGeo;

	public DataLayer() {
		connect();
		loadTwitterInstances();
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
			selectTweetsContent = getDBConnection().prepareStatement("SELECT id, content FROM tweet WHERE userid=? AND content IS NOT NULL");
			selectTweetsRawJSON = getDBConnection().prepareStatement("SELECT id, json FROM tweet WHERE userid=? AND json IS NOT NULL");
			updateTweet = getDBConnection().prepareStatement("UPDATE tweet SET json=? WHERE id=?");
			selectUnparsedTweets = getDBConnection().prepareStatement(
					"SELECT json FROM tweet WHERE parsed IS NULL OR NOT parsed LIMIT ? OFFSET ?");
			updateParsedTweet = getDBConnection().prepareStatement("UPDATE tweet SET content=?, geocoords=?,parsed=TRUE WHERE id=?");
			insertTweetUrl = getDBConnection().prepareStatement("INSERT INTO tweeturl (tweetid, originalurl,expandedurl) VALUES (?,?,?)");
			selectUserWithTimeline = getDBConnection().prepareStatement("SELECT id FROM \"user\" WHERE timelineparsed");
			selectGeoFromTweets = getDBConnection().prepareStatement("SELECT geocoords FROM tweet WHERE userid=? AND geocoords IS NOT NULL");
			selectGeo = getDBConnection().prepareStatement("SELECT json FROM geolocation WHERE lat=? AND lng=?");
			insertGeo = getDBConnection().prepareStatement("INSERT INTO geolocation (lat,lng,json) VALUES (?,?,?)");
			removeGeo = getDBConnection().prepareCall("DELETE FROM geolocation WHERE lat=? AND lng=?");
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void loadTwitterInstances() {
		try {
			File cred = new File(twitterCredentialsPath);
			if (!cred.exists() || !cred.isFile()) {
				throw new FileNotFoundException("Could not load credentials. File not found: " + twitterCredentialsPath);
			}

			BufferedReader reader = new BufferedReader(new FileReader(cred));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty() || line.startsWith("#")) continue;
				String[] a_line = line.split(",");
				TwitterAuthentication auth = new TwitterAuthentication(a_line[0].trim(), a_line[1].trim(), a_line[2].trim(), a_line[3].trim(), a_line[4].trim());
				Configuration config = getTwitterConfiguration(auth);
				Twitter tw = new TwitterFactory(config).getInstance();
				User user = tw.verifyCredentials();
				TwitterStream tws = new TwitterStreamFactory(config).getInstance();
				twitterInstances.add(tw);
				twitterStreamInstances.add(tws);
			}
			System.out.println("Loaded " + twitterInstances.size() + " twitter authentications.");
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		return getDBConnection().createStatement().executeQuery(sql);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return getDBConnection().prepareStatement(sql);
	}

	public List<String> getGeolocationFromTweets(long userId) {
		List<String> rawGeo = new ArrayList<String>();
		try {
			selectGeoFromTweets.setLong(1, userId);
			ResultSet result = selectGeoFromTweets.executeQuery();
			while (result.next()) {
				rawGeo.add(result.getString("geocoords"));
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		return rawGeo;
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
			List<TwitterParserTask> twitterParserTasks = new ArrayList<TwitterParserTask>();
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
				twitterParserTasks.add(t);
			}
			return twitterParserTasks;
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
	 * Gets the next twitter instance that can be used
	 * 
	 * @param twitter instance currently held
	 * @return
	 * @throws TwitterException
	 */
	public Twitter getNextTwitterInstance(Twitter curTwitter) throws TwitterException {
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
	 * Retrieves all the tweets from the user from the DB, and returns either the
	 * plain content or the raw json
	 * 
	 * @param userId
	 * @return A Map with tweet id, tweet content pairs
	 */
	public Map<Long, String> getUserTweets(Long userId, boolean inRawJSON) {
		Map<Long, String> tweets = new HashMap<Long, String>();
		ResultSet result;
		try {
			if (inRawJSON) {
				selectTweetsRawJSON.setLong(1, userId);
				result = selectTweetsRawJSON.executeQuery();
			}
			else {
				selectTweetsContent.setLong(1, userId);
				result = selectTweetsContent.executeQuery();
			}

			while (result.next()) {
				Long id = result.getLong(1);
				String data = result.getString(2);
				tweets.put(id, data);
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

	public String getJSONForGeolocation(String latlng) {
		// check parameter
		if (latlng == null || latlng.length() == 0) return null;
		String[] geo = latlng.split(",");
		// check for invalid format
		if (geo.length != 2) return null;
		String lat = geo[0];
		String lng = geo[1];
		return getJSONForGeolocation(lat, lng);
	}

	public String updatePrecision(String coordinate, int precision) {
		String[] scoord = coordinate.split("\\.");
		if (scoord.length == 1) return scoord[0];
		else {
			String decimals = scoord[1].length() > precision ? scoord[1].substring(0, precision) : scoord[1];
			String precisionCoord = scoord[0] + "." + decimals;
			return precisionCoord;
		}
	}

	public String getJSONForGeolocation(String lat, String lng) {
		try {
			// make sure we have at most 5 decimals
			String latPrecise = updatePrecision(lat, geoPrecisionDecimals);
			String lngPrecise = updatePrecision(lng, geoPrecisionDecimals);

			selectGeo.setString(1, latPrecise);
			selectGeo.setString(2, lngPrecise);
			ResultSet result = selectGeo.executeQuery();
			if (result.next()) {
				return result.getString("json");
			}
			else {
				return null;
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void storeGeolocation(String lat, String lng, String json) throws SQLException {
		// check parameters
		if (lat == null || lng == null || json == null) return;
		try {
			String latPrecise = updatePrecision(lat, geoPrecisionDecimals);
			String lngPrecise = updatePrecision(lng, geoPrecisionDecimals);

			insertGeo.setString(1, latPrecise);
			insertGeo.setString(2, lngPrecise);
			insertGeo.setString(3, json);
			insertGeo.executeUpdate();
			commit();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			rollback();
			throw ex;
		}
	}

	public void storeGeolocation(String latlng, String json) throws SQLException {
		// check parameters
		if (latlng == null || json == null) return;
		String[] geo = latlng.split(",");
		// check format
		if (geo.length != 2) return;
		String lat = geo[0];
		String lng = geo[1];
		storeGeolocation(lat, lng, json);
	}

	public void removeGeolocation(String latlng) {
		// check parameters
		if (latlng == null) return;
		String[] geo = latlng.split(",");
		// check format
		if (geo.length != 2) return;
		String lat = geo[0];
		String lng = geo[1];
		String latPrecise = updatePrecision(lat, geoPrecisionDecimals);
		String lngPrecise = updatePrecision(lng, geoPrecisionDecimals);
		try {
			removeGeo.setString(1, latPrecise);
			removeGeo.setString(2, lngPrecise);
			removeGeo.executeUpdate();
			commit();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			rollback();
		}
	}

	public void storeFollowing(Set<Long> userIDs, TwitterParserTask twitterParserTask) {
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
			sb.insert(0, String.format("SELECT userid FROM following WHERE taskid=%s AND userid IN (", twitterParserTask.getId()));
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
				sb.append(twitterParserTask.getId());
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
				if (ptweet.getGeocoords() != null) {
					updateParsedTweet.setString(2, ptweet.getGeocoords());
				}
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

	public void commit() throws SQLException {
		getDBConnection().commit();
	}

	private void connect() {
		try {
			Class.forName(Credentials.dbDriver);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			connection = DriverManager.getConnection(String.format("jdbc:%s://%s:%s/%s", Credentials.dbType, Credentials.dbHost, Credentials.dbPort, Credentials.dbName), Credentials.dbUsername, Credentials.dbPassword);
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

	private static Configuration getTwitterConfiguration(TwitterAuthentication auth) throws TwitterException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setJSONStoreEnabled(true).setOAuthConsumerKey(auth.consumerKey).setOAuthConsumerSecret(auth.consumerSecret).setOAuthAccessToken(
				auth.accessToken).setOAuthAccessTokenSecret(auth.accessTokenSecret).setPrettyDebugEnabled(true);
		return cb.build();
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

	public void rollback() {
		try {
			connection.rollback();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			connect();
		}
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
