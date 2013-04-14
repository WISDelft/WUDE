/**
 * 
 */
package nl.wisdelft.um;

import java.util.Map;

/**
 * @author oosterman
 */
public class UserDocument {
	private long userId;
	private Map<Long, String> tweets;
	private final String newline = System.getProperty("line.separator");

	public UserDocument(long userId, Map<Long, String> tweets) {
		this.userId = userId;
		this.tweets = tweets;
	}

	public String getDocument() {
		if (tweets == null) return "";
		StringBuilder sb = new StringBuilder();
		for (String s : tweets.values()) {
			sb.append(s);
			sb.append(newline);
		}
		return sb.toString();
	}

	public long getUserId() {
		return userId;
	}
}
