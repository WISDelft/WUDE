/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import nl.wisdelft.twitter.gatherer.TwitterConnectionManager.ConnectionMode;
import nl.wisdelft.twitter.gatherer.configuration.FriendConfiguration;
import nl.wisdelft.twitter.gatherer.configuration.OutputConfiguration;
import nl.wisdelft.twitter.gatherer.entity.TwitterFollower;
import nl.wisdelft.twitter.gatherer.repository.BaseRepository;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import twitter4j.IDs;
import twitter4j.TwitterException;
import com.google.gson.Gson;

/**
 * @author oosterman Parser to get the followers from the users indicated by the
 *         inputlist The follower relation is stored and the followers are
 *         stored as users
 */
public class FriendGathererThread extends Thread {

	private FriendConfiguration inputConfig = null;
	private OutputConfiguration outputConfig = null;
	private TwitterConnection twitterConnection = null;
	private Gson json = new Gson();
	private static String outputDirSuffix = "friends";

	public FriendGathererThread(FriendConfiguration inputConfig, OutputConfiguration outputConfig) {
		this.inputConfig = inputConfig;
		this.outputConfig = outputConfig;
	}

	@Override
	public void run() {
		System.out.println("FriendGathererThread - thread started");
		// Either get the provided connection when in SINGLE mode, or the first
		// connection from the pool in POOL mode.
		if (inputConfig.connectionMode == ConnectionMode.SINGLE) {
			twitterConnection = inputConfig.connection;
		}
		else {
			twitterConnection = inputConfig.connectionManager.getTwitterInstance();
		}

		long[] ids = inputConfig.twitterUserIDs;
		for (long id : ids) {
			// check if we need to gather the info again if on FILE mode
			if (outputConfig.toFile) {
				String file = DiskUtility.getOutputFile(outputConfig.fileDirectory, outputDirSuffix, true, id, outputConfig.zipFiles);
				File userFile = new File(file);
				if (!outputConfig.fileOverwrite && userFile.exists()) {
					// Do not gather again
					System.out.println("FriendGathererThread - User " + id + " already exists. Skipped.");
					continue;
				}
			}
			// check if we need to gather the info again if on DB mode
			else if (outputConfig.toDB) {
				// TODO implement fallback mechanism for DB
			}

			// get the ids of the friends
			long friendsIDs[] = getFriends(id);
			// store the friends
			storeFriends(id, friendsIDs);
			System.out.println("FriendGathererThread - Gathered " + friendsIDs.length + " friends from userId " + id);
			friendsIDs = null;

		}
		System.out.println("FriendGathererThread -  done");

	}

	/**
	 * Gets all the friends of (meaning users that are followed by) the provided
	 * user.
	 * 
	 * @param userFrom
	 * @return
	 */
	private long[] getFriends(long id) {
		long[] friendIDs = new long[0];
		IDs pages = null;
		boolean success = false;
		while (!success) {
			try {
				// get all the pages if there are multiple
				while (pages == null || (friendIDs.length < inputConfig.maxFriends && pages.hasNext())) {
					// get paged results
					if (pages == null) pages = twitterConnection.connection.getFriendsIDs(id, -1);
					else pages = twitterConnection.connection.getFriendsIDs(id, pages.getNextCursor());
					// add the ids to the list
					friendIDs = ArrayUtils.addAll(friendIDs, pages.getIDs());
				}
				success = true;
			}
			catch (TwitterException ex) {
				success = inputConfig.connectionManager.handleTwitterException(ex, inputConfig.connectionMode, twitterConnection);
			}
		}
		return friendIDs;
	}

	private void storeFriends(long userID, long[] friendsIDs) {
		// Stores the friends in <dir>/friends/<YYYY-MM-dd>/<userID>.json
		if (outputConfig.toFile) {
			String dir = outputConfig.fileDirectory;
			// correct for paths without trailing slash
			if (!dir.endsWith("/")) dir += "/";
			// Add the profiles directory
			dir += outputDirSuffix + "/";
			// add the current date to the directory
			Calendar c = new GregorianCalendar();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String date = format.format(c.getTime());
			String dateDir = dir + date + "/";
			// make sure the directory exists
			File dateDirFile = new File(dateDir);
			dateDirFile.mkdirs();

			// create file file based on the user ID
			String file = dateDir + userID + ".json";
			DiskUtility.writeFile(file, json.toJson(friendsIDs), outputConfig.zipFiles);

		}

		if (outputConfig.toDB) {
			Session s = HibernateUtil.getSessionFactory().getCurrentSession();
			Transaction t = s.beginTransaction();
			BaseRepository<TwitterFollower> repo = new BaseRepository<TwitterFollower>(TwitterFollower.class);
			// create hibernate entities out of the users
			for (long friend : friendsIDs) {
				// remember: friends are persons that are followed by the user in
				// question
				TwitterFollower twf = new TwitterFollower(userID, friend);
				repo.persist(twf);
			}
			t.commit();
		}

	}

	public static String getOutputDirSuffix() {
		return outputDirSuffix;
	}

	public static void setOutputDirSuffix(String outputDirSuffix) {
		FriendGathererThread.outputDirSuffix = outputDirSuffix;
	}

}
