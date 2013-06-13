/**
 * 
 */
package nl.wisdelft.parser.twitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import nl.wisdelft.text.URLShorteners;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * @author oosterman
 */
public class TweetParserThread extends Thread {

	private int batchSize = 5000;
	/**
	 * Each instance increases the static count. Needed to determine the offset
	 * for multiple TweetParserThreads
	 */
	public static int instanceCount;
	/**
	 * Instancenumber to determine the offset. Needed to determine the offset for
	 * multiple TweetParserThreads
	 */
	private int instanceNr;
	

	/**
	 * Minutes to wait when no tweets are available
	 */
	private int waitForNewTask = 5 * 60;
	private static long parsedTweets = 0;

	public TweetParserThread() {
		this.instanceNr = instanceCount++;
	}

	public boolean isShortenedUrl(String url) {
		int domainSlash = url.indexOf("/", 9);
		// if there is no trailing slash it is not a short url;
		if (domainSlash < 0) return false;
		else {
			String domain = url.substring(0, domainSlash + 1);
			return URLShorteners.isShortenedDomain(domain);
		}
	}

	

	public String removeNullChar(String s) {
		byte nullChar = '\u0000';
		byte emptyChar = ' ';
		byte[] bytes = s.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == nullChar) bytes[i] = emptyChar;
		}
		return new String(bytes);
	}

	@Override
	public void run() {
		/*Set<Status> tweets;
		System.out.println("TweetParserThread thread started with id " + Thread.currentThread().getId());
		while (true) {
			// Get the tweets which we have not processed yet
			int offset = instanceNr * batchSize;
			//tweets = datalayer.getUnparsedTweets(batchSize, offset);
			if (tweets.size() > 0) {
				// create the temporary storage
				Collection<ParsedTweet> parsedTweets = new ArrayList<ParsedTweet>();
				for (Status tweet : tweets) {
					ParsedTweet ptweet = new ParsedTweet(tweet.getId());
					// "parse" the content
					ptweet.setContent(removeNullChar(tweet.getText()));
					// parse the geocoordinates
					if (tweet.getGeoLocation() != null)
						ptweet.setGeocoords(tweet.getGeoLocation().getLatitude() + "," + tweet.getGeoLocation().getLongitude());
					// parse all urls
					URLEntity[] entities = tweet.getURLEntities();

					for (int i = 0; entities != null && i < entities.length; i++) {
						String originalurl;
						String expandedurl = null;
						if (entities[i].getExpandedURL() != null) originalurl = entities[i].getExpandedURL();
						else originalurl = entities[i].getURL();
						// check if the url is from a shortening service
						if (isShortenedUrl(originalurl)) expandedurl = URLShorteners.getUnshortenedUrl(originalurl);
						ptweet.addUrl(new ParsedUrl(originalurl, expandedurl));
					}

					// add the parsed tweet to the list
					parsedTweets.add(ptweet);
					if (++TweetParserThread.parsedTweets % 100000 == 0) {
						System.out.println(String.format("TweetParserThread parsed tweets: %s Time:%s", TweetParserThread.parsedTweets, new Date()));
					}
				}
				System.out.print("\tTweetParser thread " + Thread.currentThread().getId() + " storing " + tweets.size() + " parsed tweets...");
				// store (update) the parsed tweets
				datalayer.updateParsedTweets(parsedTweets);
				System.out.println("stored.");
			}
			else {
				try {
					System.out.println("TweetParser Thread " + Thread.currentThread().getId() + " waiting for new tasks...");
					Thread.sleep(waitForNewTask);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}*/
	}
}
