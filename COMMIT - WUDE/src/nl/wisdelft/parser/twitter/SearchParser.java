package nl.wisdelft.parser.twitter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.wisdelft.data.DataLayer;
import nl.wisdelft.data.JSONStatus;
import nl.wisdelft.data.TwitterParserTask;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SearchParser {

	private DataLayer datalayer;
	private Twitter twitter = null;
	private List<TwitterParserTask> twitterParserTasks;

	// private long joostermanId = 158663891;
	private final int maxQueryCount = 100;
	private static RateLimitStatus rateLimitSearch;
	private int waitingTimeNewTasks = 5 * 60;
	private int defaultSearchCount = 1000;

	public SearchParser() throws TwitterException {
		datalayer = new DataLayer();
		twitter = datalayer.getNextTwitterInstance(twitter);
		updateTasks();
		updateRateLimits();
		System.out.println("SearchParser thread started with id " + Thread.currentThread().getId());
		while (true) {
			if (twitterParserTasks == null || twitterParserTasks.size() == 0) {
				try {
					// sleep 10 second than try again
					System.out.println(String.format("SearchParser: Nothing to parse. Sleeping %s seconds.", waitingTimeNewTasks));
					Thread.sleep(1000 * waitingTimeNewTasks);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				// there is something to do
				System.out.println("SearchParser: Parsing " + twitterParserTasks.size() + " search queries");
				Set<JSONStatus> tweets;
				for (int i = 0; i < twitterParserTasks.size(); i++) {
					TwitterParserTask twitterParserTask = twitterParserTasks.get(i);
					Query q = createQuery(twitterParserTask);
					int count = twitterParserTask.getMaxTweets() == null ? defaultSearchCount : twitterParserTask.getMaxTweets();
					// check if we are within the rate limit for search
					int nrPlannedQueries = (int) Math.ceil(count / maxQueryCount);
					int sleep = getSecondsToSleepForExceededRateLimitSearch(nrPlannedQueries);
					if (sleep > 0) {
						try {
							Calendar calendar = Calendar.getInstance();
							calendar.add(Calendar.SECOND, sleep);
							String time = SimpleDateFormat.getTimeInstance().format(calendar.getTime());
							System.out.println(String.format("SearchParser: Rate limit exceeded. Sleeping for %s seconds. Continue at %s", sleep, time));
							Thread.sleep(1000 * sleep);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// get tweets
					tweets = getTweetsFromQuery(q, count);
					// store tweets
					datalayer.storeTweets(tweets, twitterParserTask.getId());
					// check if we need to store the users in the tweets
					if (twitterParserTask.isAddFoundUsers()) datalayer.storeFollowing(getUserIdFromTweets(tweets), twitterParserTask);
					// update task
					datalayer.setSearchTaskQueried(twitterParserTask.getId(), true);
					System.out.println(String.format("SearchParser: Parsed %s / %s queries.", i + 1, twitterParserTasks.size()));
				}
			}
			// tasks do not get added that frequently. Wait a couple of minutes and
			// update the tasks
			try {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.SECOND, waitingTimeNewTasks);
				String time = SimpleDateFormat.getTimeInstance().format(calendar.getTime());
				System.out.println("SearchParser: Getting new tasks at " + time);
				Thread.sleep(1000 * waitingTimeNewTasks);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			// get new tasks
			updateTasks();

		}
	}

	private void updateTasks() {
		// Get all Query tasks
		List<TwitterParserTask> twitterParserTasks = datalayer.getTasks();
		this.twitterParserTasks = new ArrayList<TwitterParserTask>();
		for (TwitterParserTask t : twitterParserTasks) {
			if (t.isSearch() /* && !t.isQueried() */) this.twitterParserTasks.add(t);
		}
	}

	private void updateRateLimits() {
		Map<String, RateLimitStatus> rateLimits;
		try {
			rateLimits = twitter.getRateLimitStatus("search");
			RateLimitStatus search = rateLimits.get("/search/tweets");
			rateLimitSearch = search;
		}
		catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	private int getSecondsToSleepForExceededRateLimitSearch(int nrPlannedQueries) {
		// if we are planning to go below the ratelimit
		int limitAfter = rateLimitSearch.getRemaining() - nrPlannedQueries;
		if (limitAfter <= 0) {
			// update our value from the server
			updateRateLimits();
			// if we are really out of rate
			limitAfter = rateLimitSearch.getRemaining() - nrPlannedQueries;
			if (limitAfter <= 0) {
				return rateLimitSearch.getSecondsUntilReset();
			}
		}
		// All is OK
		return 0;
	}

	/**
	 * Creates a query based on the language, geo and search string from the Task
	 * 
	 * @param twitterParserTask
	 * @return The query
	 */
	private Query createQuery(TwitterParserTask twitterParserTask) {
		Query q = new Query();
		if (twitterParserTask.getLanguage() != null) q.setLang(twitterParserTask.getLanguage());
		if (twitterParserTask.getLongitude() != null && twitterParserTask.getLatitude() != null && twitterParserTask.getRadius() != null) {
			GeoLocation geo = new GeoLocation(twitterParserTask.getLatitude(), twitterParserTask.getLongitude());
			q.setGeoCode(geo, twitterParserTask.getRadius(), Query.KILOMETERS);
		}
		if (twitterParserTask.getSearchString() != null) q.setQuery(twitterParserTask.getSearchString());

		return q;
	}

	private Set<Long> getUserIdFromTweets(Iterable<? extends Status> tweets) {
		Set<Long> ids = new HashSet<Long>();
		for (Status tweet : tweets) {
			ids.add(tweet.getUser().getId());
		}
		return ids;
	}

	private Set<JSONStatus> getTweetsFromQuery(Query query, int maxTweets) {
		try {
			// set the appropriate count
			query.setCount(Math.min(maxQueryCount, maxTweets));
			// get the next tweets
			QueryResult result = twitter.search(query);

			Set<JSONStatus> tweets = datalayer.toJSONStatus(result.getTweets());
			
			// substract the retrieved tweets from what we want
			maxTweets -= tweets.size();
			if (result.hasNext() && maxTweets > 0) {
				Query nextQuery = result.nextQuery();
				tweets.addAll(getTweetsFromQuery(nextQuery, maxTweets));
			}
			return tweets;
		}
		catch (TwitterException e) {
			e.printStackTrace();
			return new HashSet<JSONStatus>();
		}
	}
}
