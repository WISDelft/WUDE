package nl.wisdelft.parser.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import nl.wisdelft.data.JSONStatus;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.json.DataObjectFactory;

public class StreamingParser extends Thread {

	private Map<Long, List<Integer>> following;
	private StatusListener listener;
	private List<TwitterStream> streams;
	private int refreshTimeout = 60 * 10;
	private final int maxFollow = 5000;

	public StreamingParser() {
		streams = new ArrayList<TwitterStream>();
	}

	@Override
	public void run() {
		listener = new StatusListener() {
			int count = 0;

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			public void onTrackLimitationNotice(int arg0) {}
			public void onStallWarning(StallWarning arg0) {}
			public void onScrubGeo(long arg0, long arg1) {}
			public void onDeletionNotice(StatusDeletionNotice arg0) {}

			public void onStatus(Status status) {
				//convert into JSON tweet
				JSONStatus tweet = new JSONStatus(status, DataObjectFactory.getRawJSON(status));
				// get the user
				long userId = tweet.getUser().getId();

				// get the tasks that follow this user
				List<Integer> tasks = following.get(userId);
				// if we are following this user
				if (tasks != null) {
					for (Integer taskId : tasks) {
						// for each task store the tweet
						
					}
					if (++count % 1000 == 0) System.out.println(String.format("FollowingParser: Parsed %s streamed tweets", count));
				}
			}
		};
		System.out.println("FollowingParser thread started with id " + Thread.currentThread().getId());
		while (true) {
			try {
				refreshFollowers();
			}
			catch (TwitterException ex) {
				ex.printStackTrace();
			}
			try {
				Thread.sleep(1000 * refreshTimeout);
				System.out.println(String.format("FollowingParser: Refreshing users to follow."));
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void refreshFollowers() throws TwitterException {
		// stop all streams
		for (TwitterStream s : streams) {
			s.cleanUp();
		}
		streams = new ArrayList<TwitterStream>();
		// get all people to follow
		following = null;
		// if there a no people to follow
		if (following == null || following.size() == 0) return;

		long[] follow = followers();
		int index = 0;
		TwitterStream stream = null;
		FilterQuery q;
		while (follow.length > 0 && stream != null) {
			int maxIndex = Math.min(follow.length, maxFollow);
			long[] followSub = Arrays.copyOfRange(follow, 0, maxIndex);
			follow = Arrays.copyOfRange(follow, maxIndex, follow.length);
			q = new FilterQuery(followSub);
			stream.addListener(listener);
			streams.add(stream);
			stream.filter(q);
			System.out.println("FollowingParser: following " + followSub.length + " users on application " + index);
			// get new stream
			stream = null;
		}
		if (follow.length > 0) {
			System.err.println("Could not follow " + follow.length + " users.");
		}
	}

	private long[] followers() {
		if (following == null || following.size() == 0) return new long[] {};
		else {
			long[] ids = new long[following.size()];
			Iterator<Long> it = following.keySet().iterator();
			for (int i = 0; it.hasNext(); i++) {
				ids[i] = it.next();
			}
			return ids;
		}
	}

}