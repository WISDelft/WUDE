/**
 * 
 */
package nl.wisdelft.twitter.gatherer.configuration;

/**
 * @author oosterman
 */
public class AppConfiguration {
	public boolean getFriends = false;
	public int friendsMaxCount = 5000;
	public boolean getFollowers = false;
	public int followersMaxCount = 5000;
	public boolean getProfile = false;
	public boolean getTweets = false;
	public boolean toFile = false;
	public String fileDir = null;
	public boolean zipFiles = false;
	public boolean toDB = false;
	public long[] ids = new long[]{};
	public boolean fileOverwrite = false;
}
