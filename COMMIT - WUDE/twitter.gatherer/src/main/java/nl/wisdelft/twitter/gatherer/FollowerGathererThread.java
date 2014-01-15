/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.File;
import nl.wisdelft.twitter.gatherer.TwitterConnectionManager.ConnectionMode;
import nl.wisdelft.twitter.gatherer.configuration.FollowerConfiguration;
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
public class FollowerGathererThread extends Thread {
	private FollowerConfiguration inputConfig = null;
	private OutputConfiguration outputConfig = null;
	private TwitterConnection twitterConnection = null;
	private Gson json = new Gson();
	private static String outputDirSuffix = "followers";

	public FollowerGathererThread(FollowerConfiguration inputConfig, OutputConfiguration outputConfig) {
		this.inputConfig = inputConfig;
		this.outputConfig = outputConfig;
	}

	@Override
	public void run() {
		System.out.println("FollowerGathererThread- thread started");
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
					System.out.println("FollowerGathererThread - User " + id + " already exists. Skipped.");
					continue;
				}
			}
			// check if we need to gather the info again if on DB mode
			else if (outputConfig.toDB) {
				// TODO implement fallback mechanism for DB
			}

			// get the ids of the friends
			long followersIDs[] = getFollowers(id);
			// store the friends
			storeFollowers(id, followersIDs);
			System.out.println("FollowerGathererThread - Gathered " + followersIDs.length + " followers from userId " + id);
			followersIDs = null;

		}
		System.out.println("FollowerGathererThread -  done");

	}

	/**
	 * Gets all the followers of (meaning users that follow) the provided user.
	 * 
	 * @param id
	 * @return
	 */
	private long[] getFollowers(long id) {
		long[] followerIDs = new long[0];
		IDs pages = null;
		boolean success = false;
		while (!success) {
			try {
				// get all the pages if there are multiple
				while (pages == null || (followerIDs.length < inputConfig.maxFollowers && pages.hasNext())) {
					// get paged results
					if (pages == null) pages = twitterConnection.connection.getFollowersIDs(id, -1);
					else pages = twitterConnection.connection.getFollowersIDs(id, pages.getNextCursor());
					// add the ids to the list
					followerIDs = ArrayUtils.addAll(followerIDs, pages.getIDs());
				}
				success = true;
			}
			catch (TwitterException ex) {
				success = inputConfig.connectionManager.handleTwitterException(ex, inputConfig.connectionMode, twitterConnection);
			}

		}
		return followerIDs;
	}

	private void storeFollowers(long userID, long[] followersIDs) {
		// Stores the friends in <dir>/friends/<YYYY-MM-dd>/<userID>.json
		if (outputConfig.toFile) {
			String outputDir = DiskUtility.getOutputDirectory(outputConfig.fileDirectory, outputDirSuffix, true);
			// make sure the directory exists
			File dateDirFile = new File(outputDir);
			dateDirFile.mkdirs();

			// create file file based on the user ID
			String file = outputDir + userID + ".json";
			DiskUtility.writeFile(file, json.toJson(followersIDs), outputConfig.zipFiles);
		}

		if (outputConfig.toDB) {
			Session s = HibernateUtil.getSessionFactory().getCurrentSession();
			Transaction t = s.beginTransaction();
			BaseRepository<TwitterFollower> repo = new BaseRepository<TwitterFollower>(TwitterFollower.class);
			// create hibernate entities out of the users
			for (long follower : followersIDs) {
				// remember: the follower follows the userin question
				TwitterFollower twf = new TwitterFollower(follower, userID);
				repo.persist(twf);
			}
			t.commit();
		}

	}

	public static String getOutputDirSuffix() {
		return outputDirSuffix;
	}

	public static void setOutputDirSuffix(String outputDirSuffix) {
		FollowerGathererThread.outputDirSuffix = outputDirSuffix;
	}

}
