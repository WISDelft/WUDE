import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.wisdelft.data.DataLayer;
import nl.wisdelft.text.Emoticons;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.json.DataObjectFactory;

/**
 * 
 */

/**
 * @author oosterman
 */
public class Statistics {
	final static String newline = System.getProperty("line.separator");

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		System.out.println("System started at " + new Date());

		calculateFeatures();

		System.out.println("System finished in " + (System.currentTimeMillis() - start) / 1000 / 60 + " minutes");
	}

	private static void calculateFeatures() throws Exception {
		// loop trough all the raw JSON users
		String path = "/Users/oosterman/Documents/Data/rawJSONTwitterUser/nl";
		String output = "/Users/oosterman/Documents/Data/output/features.csv";
		File result = new File(output);
		BufferedWriter writer = new BufferedWriter(new FileWriter(result));
		//write header line
		String header = "userId,nrHashtags,nrMentions,nrRT,nrTweetsInDataset,nrTweetsWithGeo,nrTweetsWithPunctuation,nrTweetsWithSmiley,nrUrlInTweets,followerCount,friendsCount,listedCount,statusesCount,verified,urlInProfile";
		writer.write(header);
		File dir = new File(path);
		if (!dir.exists() || !dir.isDirectory()) throw new FileNotFoundException("Directory not found");
		DataLayer datalayer = new DataLayer();

		Pattern punctuation = Pattern.compile("^.*(\\.|\\!|\\?){3}.*$");
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (!file.isFile() || !file.canRead()) continue;
			long userId = Long.parseLong(file.getName());
			// read in the file
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(newline);
			}
			reader.close();
			String content = builder.toString();
			// create twitter object
			User user = DataObjectFactory.createUser(content);

			// get the features from the user
			// User Features
			int followerCount = user.getFollowersCount();
			int statusesCount = user.getStatusesCount();
			int friendsCount = user.getFriendsCount();
			int listedCount = user.getListedCount();
			int verified = user.isVerified() ? 1 : 0;
			int urlInProfile = user.getURL() != null && user.getURL().startsWith("http") ? 1 : 0;

			// Tweet features
			int nrTweetsInDataset = 0;
			int nrRT = 0;
			int nrMentions = 0;
			int nrHashtags = 0;
			int nrUrlInTweets = 0;
			int nrTweetsWithGeo = 0;
			int nrTweetsWithPunctuation = 0;
			int nrTweetsWithSmiley = 0;

			// get all the tweets for this user
			Map<Long, String> tweets = datalayer.getUserTweets(userId, true);
			nrTweetsInDataset = tweets.size();
			for (String json : tweets.values()) {
				Status tweet = DataObjectFactory.createStatus(json);
				if (tweet.isRetweet()) nrRT++;
				if (tweet.getUserMentionEntities() != null) nrMentions += tweet.getUserMentionEntities().length;
				if (tweet.getHashtagEntities() != null) nrHashtags += tweet.getHashtagEntities().length;
				if (tweet.getURLEntities() != null) nrUrlInTweets += tweet.getURLEntities().length;
				if (tweet.getGeoLocation() != null) nrTweetsWithGeo++;

				Matcher m = punctuation.matcher(tweet.getText());
				if (m.matches()) nrTweetsWithPunctuation++;

				// tokenize and check for smileys
				String[] words = tweet.getText().split("\\s+");
				for (String word : words) {
					if (Emoticons.isEmoticon(word)) {
						nrTweetsWithSmiley++;
						break;
					}
				}
			}
			//store the found values
			String data = userId+","+nrHashtags+","+nrMentions+","+nrRT+","+nrTweetsInDataset+","+nrTweetsWithGeo+","+nrTweetsWithPunctuation+","+nrTweetsWithSmiley+","+nrUrlInTweets+","+followerCount+","+friendsCount+","+listedCount+","+statusesCount+","+verified+","+urlInProfile;
			writer.write(data);
			writer.newLine();
			writer.flush();
			System.out.println("User "+i+"/"+files.length);
			
		}
		writer.close();
	}

	private static void fixGeoLocations() throws SQLException {
		DataLayer datalayer = new DataLayer();
		String sql = "SELECT lat,lng FROM geolocation";
		ResultSet result = datalayer.executeQuery(sql);
		PreparedStatement psql = datalayer.prepareStatement("UPDATE geolocation SET lat=?, lng=? WHERE lat=? AND lng=?");
		PreparedStatement psqld = datalayer.prepareStatement("DELETE FROM geolocation WHERE lat=? AND lng=?");
		int i = 0;
		while (result.next()) {
			String lat = result.getString("lat");
			String lng = result.getString("lng");
			String latPrecision = datalayer.updatePrecision(lat, 4);
			String lngPrecision = datalayer.updatePrecision(lng, 4);
			// nothing to do
			if (lat.length() == latPrecision.length() && lng.length() == lngPrecision.length()) continue;
			try {
				psql.setString(1, latPrecision);
				psql.setString(2, lngPrecision);
				psql.setString(3, lat);
				psql.setString(4, lng);
				psql.executeUpdate();
				datalayer.commit();
				System.out.println(++i);
			}
			catch (SQLException ex) {
				if (ex.getMessage().contains("duplicate")) {
					// remove the duplicate
					datalayer.rollback();
					psqld.setString(1, lat);
					psqld.setString(2, lng);
					psqld.executeUpdate();
					datalayer.commit();
				}
				else {
					ex.printStackTrace();
				}

			}
		}
	}
}
