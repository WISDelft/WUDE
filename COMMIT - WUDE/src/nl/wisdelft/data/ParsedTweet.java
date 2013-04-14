/**
 * 
 */
package nl.wisdelft.data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author oosterman
 *
 */
public class ParsedTweet {
	private long tweetId;
	private String content;
	private String geocoords;
	private Set<ParsedUrl> urls;

	public ParsedTweet(long tweetId) {
		this.tweetId = tweetId;
		urls = new HashSet<ParsedUrl>();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTweetId() {
		return tweetId;
	}

	public void addUrl(ParsedUrl url) {
		this.urls.add(url);
	}

	public Set<ParsedUrl> getUrls() {
		return urls;
	}

	public String getGeocoords() {
		return geocoords;
	}

	public void setGeocoords(String geocoords) {
		this.geocoords = geocoords;
	}
}