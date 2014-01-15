package nl.wisdelft.twitter.gatherer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.wisdelft.twitter.gatherer.TwitterConnectionManager.ConnectionMode;
import nl.wisdelft.twitter.gatherer.configuration.OutputConfiguration;
import nl.wisdelft.twitter.gatherer.configuration.TimelineConfiguration;
import nl.wisdelft.twitter.gatherer.entity.TwitterStatus;
import nl.wisdelft.twitter.gatherer.repository.BaseRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import com.google.gson.Gson;

public class TimelineGathererThread extends Thread {
	private TimelineConfiguration inputConfig = null;
	private OutputConfiguration outputConfig = null;
	private TwitterConnection twitterConnection = null;
	private static int MAXTWEETSPERPAGE = 200;
	private Gson json = new Gson();
	private static String outputDirSuffix = "tweets";

	public TimelineGathererThread(TimelineConfiguration inputConfig, OutputConfiguration outputConfig) {
		this.inputConfig = inputConfig;
		this.outputConfig = outputConfig;
	}

	@Override
	public void run() {
		super.run();
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
				String file = DiskUtility.getOutputFile(outputConfig.fileDirectory, outputDirSuffix, false, id, outputConfig.zipFiles);
				File userFile = new File(file);
				if (!outputConfig.fileOverwrite && userFile.exists()) {
					// Do not gather again
					System.out.println("TimelineGathererThread - User " + id + " already exists. Skipped.");
					continue;
				}
			}
			// check if we need to gather the info again if on DB mode
			else if (outputConfig.toDB) {
				// TODO implement fallback mechanism for DB
			}
			// get tweets of this user
			Set<TwitterStatus> tweets = getTimelineTweets(id);
			// store the tweets
			storeTweets(id, tweets);
			System.out.println("TimelineGathererThread - Gathered " + tweets.size() + " tweets from userId " + id);
			tweets = null;

		}
		System.out.println(String.format("TimelineGathererThread - done."));
	}

	/**
	 * Get all tweets that we can get from the timeline of the user using
	 * pagination
	 * 
	 * @param userId
	 * @return
	 */
	private Set<TwitterStatus> getTimelineTweets(long userId) {
		Set<TwitterStatus> timeline = new HashSet<TwitterStatus>();
		Paging paging;
		int pagenr = 1;
		boolean done = false;
		while (!done) {
			boolean success = false;
			paging = new Paging(pagenr, MAXTWEETSPERPAGE);
			while (!success) {
				try {
					List<Status> t = twitterConnection.connection.getUserTimeline(userId, paging);
					// put into our container
					Set<TwitterStatus> tweets = HibernateUtil.toTwitterStatus(t);
					// Stopping condition: we retrieved tweets
					if (tweets.size() > 0) {
						timeline.addAll(tweets);
						pagenr++;
					}
					else {
						done = true;
					}
					success = true;
				}
				catch (TwitterException ex) {
					success = inputConfig.connectionManager.handleTwitterException(ex, inputConfig.connectionMode, twitterConnection);
					// if we have a recoverable error (success = false) retry, else we are
					// done.
					done = success;
				}
			}
		}
		return timeline;
	}

	private void storeTweets(long userID, Set<TwitterStatus> tweets) {
		// Stores the profiles in <dir>/tweets/<userID>.json
		if (outputConfig.toFile) {
			String dir = outputConfig.fileDirectory;
			// correct for paths without trailing slash
			if (!dir.endsWith("/")) dir += "/";
			// Add the profiles directory
			dir += outputDirSuffix + "/";
			// make sure the directory exists
			File dirFile = new File(dir);
			dirFile.mkdirs();

			// create a file containing the JSON of all the tweets
			String file = dir + userID + ".json";
			DiskUtility.writeFile(file, json.toJson(tweets), outputConfig.zipFiles);
		}

		if (outputConfig.toDB) {
			Session s = HibernateUtil.getSessionFactory().getCurrentSession();
			Transaction t = s.beginTransaction();
			BaseRepository<TwitterStatus> repo = new BaseRepository<TwitterStatus>(TwitterStatus.class);
			for (TwitterStatus tweet : tweets) {
				repo.persist(tweet);
			}
			t.commit();
		}
	}

	public static String getOutputDirSuffix() {
		return outputDirSuffix;
	}

	public static void setOutputDirSuffix(String outputDirSuffix) {
		TimelineGathererThread.outputDirSuffix = outputDirSuffix;
	}

}
