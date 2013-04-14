/**
 * 
 */
package nl.wisdelft.parser.twitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.wisdelft.data.DataLayer;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.json.DataObjectFactory;

/**
 * @author oosterman
 */
public class TwitterUserParser {
	public static String userdocumentsDir = "/Users/oosterman/Documents/Data/userdocumentsNL";
	public static String outputDirRaw = "/Users/oosterman/Documents/Data/rawJSONTwitterUser";
	//public static String outputDirCompiled = "/Users/oosterman/Documents/Data/twitterUser/nl";

	public static Twitter twitter = null;
	public static int batchSize = 100;

	public static void main(String[] args) throws Exception {
		new TwitterUserParser();
	}

	public TwitterUserParser() throws Exception {
		DataLayer datalayer = new DataLayer();
		twitter = datalayer.getNextTwitterInstance(twitter);

		// get the files ready for parsing
		File dir = new File(userdocumentsDir);
		String[] files = dir.list();
		Set<String> availableFiles = new HashSet<String>(Arrays.asList(files));
		// get the id's we already parsed
		File parsedDir = new File(outputDirRaw);
		String[] parsedFiles = parsedDir.list();
		Set<String> parsed = new HashSet<String>(Arrays.asList(parsedFiles));

		int count = 0;
		List<Long> idsList = new ArrayList<Long>();
		for (String filename : availableFiles) {
			if (!parsed.contains(filename)) {
				idsList.add(Long.parseLong(filename));
			}
			// store when batch full
			if (idsList.size() == batchSize) {
				long[] ids = new long[idsList.size()];
				for (int i = 0; i < idsList.size(); i++) {
					ids[i] = (long) idsList.get(i);
				}
				processUserIds(ids);
				// clear the list
				idsList.clear();
			}
			System.out.println(++count+"/" +availableFiles.size());
		}
		// process the rest (the ones < batch size)
		if (idsList.size() > 0) {
			long[] ids = new long[idsList.size()];
			for (int i = 0; i < idsList.size(); i++) {
				ids[i] = (long) idsList.get(i);
			}
			processUserIds(ids);
		}
	}

	private void processUserIds(long[] ids) throws Exception {
		boolean success = false;
		ResponseList<User> users = null;
		while (!success) {
			try {
				users = twitter.lookupUsers(ids);
				success = true;
			}
			catch (TwitterException ex) {
				if (ex.exceededRateLimitation()) {
					int secondsTillReset = ex.getRateLimitStatus().getSecondsUntilReset();
					System.out.println("Sleeping till reset: " + secondsTillReset + " seconds.");
					Thread.sleep(1000 * (secondsTillReset + 5));
				}
				// Page does not exist
				else if (ex.getErrorCode() == 34) {
					System.out.println("User unknown: " + ids[0]);
					return;
				}
				else {
					ex.printStackTrace();
				}
			}
		}
		BufferedWriter rawWriter = null;
		BufferedWriter compiledWriter = null;
		for (User user : users) {
			// store the raw user json
			String rawUserJSON = DataObjectFactory.getRawJSON(user);
			File raw = new File(outputDirRaw + "/" + user.getId());
			rawWriter = new BufferedWriter(new FileWriter(raw));
			rawWriter.write(rawUserJSON);
			rawWriter.close();

			// store the compiled information
			/*
			String location = user.getLocation();
			String lang = user.getLang();
			String description = user.getDescription();
			String name = user.getName();
			Date createdAt = user.getCreatedAt();
			boolean geoEnabled = user.isGeoEnabled();
			String url = user.getURL();
			long id = user.getId();
			String screenName = user.getScreenName();
			int statusesCount = user.getStatusesCount();
			String compiledUser = String.format(
					"name:%s\nscreen name:%s\ndescription:%s\nlocation:%s\nlang:%s\nurl:%s\nstatuses count:%s\ngeo?%s\ncreated at:%s", name,
					screenName, description, location, lang, url, statusesCount, geoEnabled, createdAt);
			File compiled = new File(outputDirCompiled + "/" + id);
			compiledWriter = new BufferedWriter(new FileWriter(compiled));
			compiledWriter.write(compiledUser);
			compiledWriter.close();
			*/
		}
	}
}
