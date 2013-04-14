/**
 * 
 */
package nl.wisdelft.parser.twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nl.wisdelft.data.DataLayer;
import twitter4j.IDs;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/*
 * 1 follows 2 isfollowedby 3 mentions
 */

/**
 * @author oosterman Parser to get the followers from the users indicated by the
 *         inputlist The follower relation is stored and the followers are
 *         stored as users
 */
public class FollowerFriendParserThread extends Thread {

	public String inputFile = null;
	public String outputDirectoryFriends = "/Users/oosterman/Documents/Data/friends/";
	public String outputDirectoryFollowers = "/Users/oosterman/Documents/Data/followers/";
	private static Twitter twitter = null;
	private static DataLayer datalayer = null;
	private RateLimitStatus limitFriends = null;
	private RateLimitStatus limitFollowers = null;
	List<Long> ids = new ArrayList<Long>();

	private void updateRateLimits() {
		Map<String, RateLimitStatus> rateLimits;
		try {
			rateLimits = twitter.getRateLimitStatus("friends,followers");
			limitFriends = rateLimits.get("/friends/ids");
			limitFollowers = rateLimits.get("/followers/ids");
		}
		catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws TwitterException, InterruptedException {
		String inputFile = null;
		String outputDir = null;
		if(args.length>=2 && args[0].equals("-i"))
			inputFile = args[1];
		
		FollowerFriendParserThread t = new FollowerFriendParserThread(inputFile);
		t.run();
	}

	private void readUserIdsFromFile(String filepath) throws IOException {
		File file = new File(filepath);
		if (!file.exists() || !file.isFile()) throw new FileNotFoundException("File not found: " + filepath);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = reader.readLine()) != null) {
			long id = Long.parseLong(line);
			ids.add(id);
		}
		reader.close();
	}

	public FollowerFriendParserThread(String inputFile) throws TwitterException {
		this.inputFile = inputFile;
		datalayer = new DataLayer();
		// twitter = datalayer.getNextTwitterInstance(twitter);
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setJSONStoreEnabled(true).setOAuthConsumerKey("LEqVg6nMZbY4HYf4OjU0bQ").setOAuthConsumerSecret(
				"7iXgrTBmRuHOctDldP004KgVXljEqa0fzZDxKkcP0I").setOAuthAccessToken("158663891-FDcldMm1x7Nm2oED0VQzCBCvdIOA0IqsJr2W51pP").setOAuthAccessTokenSecret(
				"bSdUBqqTIZGDnKh8j2a7kx5kyOKleD3EwoVKMKqDgtc").setPrettyDebugEnabled(true);
		TwitterFactory twf = new TwitterFactory(cb.build());
		twitter = twf.getInstance();
		updateRateLimits();
	}

	private void getFriends(long id) throws TwitterException {
		boolean success = false;
		long[] friends = new long[]{};
		while (!success) {
			try {
				IDs friendsIds = twitter.getFriendsIDs(id, -1);
				friends = friendsIds.getIDs();
				limitFriends = friendsIds.getRateLimitStatus();
				System.out.println("\tFound " + friends.length + " friends");
				success = true;
			}
			catch (TwitterException ex) {
				success = handleTwitterException(ex);
			}
		}
		// store the friends
		storeIds(friends, outputDirectoryFriends + id);
	}

	private void getFollowers(long id) throws TwitterException {
		boolean success = false;
		long[] followers = new long[] {};
		while (!success) {
			try {
				IDs followerdIds = twitter.getFollowersIDs(id, -1);
				followers = followerdIds.getIDs();
				limitFollowers = followerdIds.getRateLimitStatus();
				System.out.println("\tFound " + followers.length + " followers");
				success = true;
			}
			catch (TwitterException ex) {
				success = handleTwitterException(ex);
			}
		}
		// store the friends
		storeIds(followers, outputDirectoryFollowers + id);
	}

	/**
	 * @param ex
	 * @return whether the exception can be seen as a success
	 */
	private boolean handleTwitterException(TwitterException ex) {
		// handle rate limit exceptions
		if (ex.exceededRateLimitation()) {
			try {
				if (ex.getRateLimitStatus().getSecondsUntilReset() < 60) {
					// if there is less then 60 seconds left just wait
					System.out.print("Limit! We wait " + ex.getRateLimitStatus().getSecondsUntilReset() + " seconds.");
					Thread.sleep(1000 * (ex.getRateLimitStatus().getSecondsUntilReset() + 1));
				}
				else {
					// else change connection and wait 30 seconds
					System.out.print("Limit! " + ex.getRateLimitStatus().getSecondsUntilReset() + " seconds till reset. ");
					twitter = datalayer.getNextTwitterInstance(twitter);
					System.out.println("New connection: " + twitter.getId());
					Thread.sleep(1000 * 30);
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch (TwitterException e2) {
				e2.printStackTrace();
				System.exit(1);
			}
			// request failed
			return false;
		}
		// the user does not exist or has a private profile
		else if (ex.getStatusCode() == 404 || ex.getStatusCode() == 401) {
			//the best we can get --> request success
			return true;
		}
		//twitter servers under heavy load
		else if(ex.getStatusCode() == 503){
			//request failed
			return false;
		}
		//network error or twitter internal server error 
		else if(ex.isCausedByNetworkIssue() || ex.getStatusCode()==500){
			//request failes
			return false;
		}
		else {
			ex.printStackTrace();
			System.exit(1);
			return false;
		}
	}

	private void storeIds(long[] ids, String path) {
		File file = new File(path);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (long id : ids) {
				writer.write(Long.toString(id));
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			System.out.println("FollowerFriendParser thread started with id " + Thread.currentThread().getId());
			if(inputFile==null){
				System.out.println("no inputfile found");
				return;
			}
			// read input file
			readUserIdsFromFile(inputFile);
			if (ids.size() == 0) {
				System.out.println("No twitter ids found in file.");
				return;
			}
			// for each user determine if we have the friends and followers
			// if not, get them from twitter

			for (int i = 0; i < ids.size(); i++) {
				long id = ids.get(i);
				File friendFile = new File(outputDirectoryFriends + id);
				if (!friendFile.exists()) getFriends(id);
				File followerFile = new File(outputDirectoryFollowers + id);
				if (!followerFile.exists()) getFollowers(id);
				System.out.println("\tParsed " + (i + 1) + "/" + ids.size());
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}
}
