/**
 * Gets the profile (in JSON) of the specified twitter user IDs and stores them
 * according the output configuration DB: table `twitteruserprofile`, columns
 * [twitteruserid long, dateretrieved timestamp, profile varchar,
 * PK(twitteruserid,dateretrieved) FILE: <dir>/<date>/<twitteruserid>.json
 */
package nl.wisdelft.twitter.gatherer;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import nl.wisdelft.twitter.gatherer.TwitterConnectionManager.ConnectionMode;
import nl.wisdelft.twitter.gatherer.configuration.OutputConfiguration;
import nl.wisdelft.twitter.gatherer.configuration.ProfileConfiguration;
import nl.wisdelft.twitter.gatherer.entity.TwitterUser;
import nl.wisdelft.twitter.gatherer.repository.BaseRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * @author oosterman
 */
public class ProfileGathererThread extends Thread {
	private ProfileConfiguration inputConfig = null;
	private OutputConfiguration outputConfig = null;
	private TwitterConnection twitterConnection = null;
	private final int USERS_LOOKUP_MAX = 100;
	private static String outputDirSuffix = "profiles";

	public ProfileGathererThread(ProfileConfiguration inputConfig, OutputConfiguration outputConfig) {
		this.inputConfig = inputConfig;
		this.outputConfig = outputConfig;
	}

	@Override
	public void run() {
		System.out.println("ProfileGathererThread - thread started");
		// Either get the provided connection when in SINGLE mode, or the first
		// connection from the pool in POOL mode.
		if (inputConfig.connectionMode == ConnectionMode.SINGLE) {
			twitterConnection = inputConfig.connection;
		}
		else {
			twitterConnection = inputConfig.connectionManager.getTwitterInstance();
		}
		// use the users/lookup operation to get the profile from multiple users in
		// bulk
		int index = 0;
		long[] ids = inputConfig.twitterUserIDs;
		while (index < ids.length) {
			// get the first USERS_LOOKUP_MAX ids, or the complete list when length <
			// USERS_LOOKUP_MAX
			long[] idsSubset;
			if (ids.length - index >= USERS_LOOKUP_MAX) {
				idsSubset = Arrays.copyOfRange(ids, index, index + 100);
			}
			else {
				idsSubset = Arrays.copyOfRange(ids, index, ids.length);
			}

			Set<TwitterUser> users = null;
			boolean success = false;
			while (!success) {
				try {
					// Get the (at most) USERS_LOOKUP_MAX profiles
					ResponseList<User> u = twitterConnection.connection.lookupUsers(idsSubset);
					users = HibernateUtil.toTwitterUser(u);
					success = true;
				}
				catch (TwitterException ex) {
					// handles the exception (potentially changing Twitter connection) and
					// indicating whether is was a success or that we should try again.
					success = inputConfig.connectionManager.handleTwitterException(ex, inputConfig.connectionMode, twitterConnection);
				}
			}

			// users now contains the batch of user profiles. Store them.
			if (users != null && users.size() > 0) storeProfiles(users);

			users = null;
			// increment the index
			index += USERS_LOOKUP_MAX;
		}
		System.out.println("ProfileGathererThread - done.");
	}

	private void storeProfiles(Set<TwitterUser> users) {
		// Stores the profiles in <dir>/profiles/<yyyy-MM-dd>/<userID>.json
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

			// create a file containing the JSON for each user
			for (TwitterUser user : users) {
				// create file file based on the user ID
				String file = dateDir + user.getUserId() + ".json";
				DiskUtility.writeFile(file, user.getRawJSON(), outputConfig.zipFiles);
			}
		}

		if (outputConfig.toDB) {
			Session s = HibernateUtil.getSessionFactory().getCurrentSession();
			Transaction t = s.beginTransaction();
			BaseRepository<TwitterUser> repo = new BaseRepository<TwitterUser>(TwitterUser.class);
			for (TwitterUser user : users) {
				repo.persist(user);
			}
			t.commit();
		}
	}

	public static String getOutputDirSuffix() {
		return outputDirSuffix;
	}

	public static void setOutputDirSuffix(String outputDirSuffix) {
		ProfileGathererThread.outputDirSuffix = outputDirSuffix;
	}
}
