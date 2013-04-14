package nl.wisdelft.parser.twitter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.wisdelft.data.DataLayer;
import nl.wisdelft.data.JSONStatus;
import nl.wisdelft.data.TimelineParserTask;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TimelineParserThread extends Thread {
	private static int timelineSize = 3000;
	private int maxGetUserTimelineCount = 200;
	private int waitingTimeNewTasks = 10;
	private int parsedTimelines = 0;

	private DataLayer datalayer;
	private Twitter twitter = null;
	private List<TimelineParserTask> tasks;
	private RateLimitStatus rateLimitStatus;

	TimelineParserThread() {}

	@Override
	public void run() {
		datalayer = new DataLayer();
		try {
			twitter = datalayer.getNextTwitterInstance(twitter);
		}
		catch (TwitterException ex) {
			// we could not get a twitterinstance. Signal and close thread.
			System.err.println(ex.toString());
			return;
		}
		// get the tasks
		tasks = datalayer.getTimelineTasks();
		updateRateLimits();
		System.out.println("TimelineParserThread thread started with id " + Thread.currentThread().getId());
		while (true) {
			// check if there are tasks
			if (tasks == null || tasks.size() == 0) {
				try {
					// sleep 10 second than try again
					System.out.println(String.format("TimelineParserThread: Nothing to parse. Sleeping %s seconds.", waitingTimeNewTasks));
					Thread.sleep(1000 * waitingTimeNewTasks);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// process the tasks
			else {
				System.out.println("TimelineParserThread: Parsing " + tasks.size() + " timelines");
				Set<JSONStatus> tweets;
				for (int i = 0; i < tasks.size(); i++) {
					TimelineParserTask task = tasks.get(i);
					// get tweets
					tweets = getTimelineTweets(task.getUserId());
					// store tweets
					boolean success = datalayer.storeTweets(tweets, task.getTaskId());
					// update user
					if (success) {
						datalayer.setUserTimelineParsed(task.getUserId(), true);
						parsedTimelines++;
						System.out.println(String.format("TimelineParserThread: Parsed timelines: %s/%s", parsedTimelines, tasks.size()));
					}
				}
			}
			// get new tasks
			tasks = datalayer.getTimelineTasks();
		}
	}

	private void updateRateLimits() {
		Map<String, RateLimitStatus> rateLimits;
		try {
			rateLimits = twitter.getRateLimitStatus("statuses");
			RateLimitStatus search = rateLimits.get("/statuses/user_timeline");
			rateLimitStatus = search;
		}
		catch (TwitterException e) {
			e.printStackTrace();
			rateLimitStatus = null;
		}
	}

	private Set<JSONStatus> getTimelineTweets(long userId) {
		try {
			Set<JSONStatus> timeline = null;
			Paging paging;
			int tweetsToGet = timelineSize;
			for (int i = 1; tweetsToGet > 0; i++) {
				paging = new Paging(i, maxGetUserTimelineCount);
				// if we have exceeded the ratelimits
				while (rateLimitStatus == null || rateLimitStatus.getRemaining() <= 0) {
					try {
						// switch to a new twitterinstance
						twitter = datalayer.getNextTwitterInstance(twitter);
						// update rate limits for this new instance
						updateRateLimits();
						//if we have still exceeded the ratelimits
						if (rateLimitStatus==null || rateLimitStatus.getRemaining() <= 0) {
							//sleep until the reset
							int sleep = rateLimitStatus == null ? 60 : rateLimitStatus.getSecondsUntilReset() + 5;
							Calendar calendar = Calendar.getInstance();
							calendar.add(Calendar.SECOND, sleep);
							String time = SimpleDateFormat.getTimeInstance().format(calendar.getTime());
							System.out.println(String.format("TimelineParserThread: Rate limit exceeded. Sleeping for %s seconds. Continue at %s", sleep,
									time));
							Thread.sleep(1000 * sleep);
							updateRateLimits();
						}
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// we have an active connection and within the limits --> do the work
				ResponseList<Status> tweets = twitter.getUserTimeline(userId, paging);
				// update the ratelimitstatus
				rateLimitStatus = tweets.getRateLimitStatus();

				Set<JSONStatus> jsonTweets = datalayer.toJSONStatus(tweets);
				if (timeline == null) timeline = jsonTweets;
				else timeline.addAll(jsonTweets);
				tweetsToGet -= maxGetUserTimelineCount;

				// stop trying if we do not get any tweets back
				if (tweets.size() == 0) break;
			}
			return timeline;
		}
		catch (TwitterException e) {
			e.printStackTrace();
			if (e.exceededRateLimitation()) updateRateLimits();
			return new HashSet<JSONStatus>();
		}
	}
}
