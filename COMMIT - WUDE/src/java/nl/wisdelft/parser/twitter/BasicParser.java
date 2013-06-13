/**
 * 
 */
package nl.wisdelft.parser.twitter;

import java.io.IOException;
import nl.wisdelft.data.Twitter4J;
import nl.wisdelft.data.entity.TwitterUser;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * @author oosterman
 */
public class BasicParser {

	private Twitter twitter = null;
	private Twitter4J twitter4j = null;

	public BasicParser() throws IOException, TwitterException {
		twitter4j = Twitter4J.getInstance();
		twitter = twitter4j.getTwitterInstance();
	}

	public TwitterUser getUser(long id) {
		boolean success = false;
		TwitterUser twUser = null;
		while (!success) {
			try {
				User u = twitter.showUser(id);
				success = true;
				twUser = Twitter4J.toTwitterUser(u);
			}
			catch (TwitterException ex) {
				success = handleTwitterException(ex);
			}
		}
		return twUser;
	}

	/**
	 * Determines whether a twitterexception can be seen a success and sleeps the
	 * thread or changed connection when necessary
	 * 
	 * @param ex The exception
	 * @return whether the exception can be seen as a success
	 */
	private boolean handleTwitterException(TwitterException ex) {
		// handle rate limit exceptions
		if (ex.exceededRateLimitation()) {
			try {
				if (ex.getRateLimitStatus().getSecondsUntilReset() < 20) {
					// if there is less then 60 seconds left just wait
					System.out.print("Rate Limit! We wait " + ex.getRateLimitStatus().getSecondsUntilReset() + " seconds.");
					Thread.sleep(1000 * (ex.getRateLimitStatus().getSecondsUntilReset() + 1));
				}
				else {
					// else change connection and wait
					System.out.print("Rate Limit! Getting new connection...");
					twitter = twitter4j.getTwitterInstance(twitter);
					System.out.println("New connection: " + twitter.getId());
					Thread.sleep(1000 * 10);
				}
			}
			catch (Exception ex2) {
				ex2.printStackTrace();
			}
			// request failed
			return false;
		}
		// the user does not exist or has a private profile
		else if (ex.getStatusCode() == 404 || ex.getStatusCode() == 401) {
			// the best we can get for this request --> request success
			return true;
		}
		// twitter servers under heavy load
		else if (ex.getStatusCode() == 503) {
			// request failed
			return false;
		}
		// network error or twitter internal server error
		else if (ex.isCausedByNetworkIssue() || ex.getStatusCode() == 500) {
			// request failed
			return false;
		}
		else {
			ex.printStackTrace();
			return false;
		}
	}

}
