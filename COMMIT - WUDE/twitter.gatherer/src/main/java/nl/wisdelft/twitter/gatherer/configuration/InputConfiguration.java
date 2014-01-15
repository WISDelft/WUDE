/**
 * 
 */
package nl.wisdelft.twitter.gatherer.configuration;

import nl.wisdelft.twitter.gatherer.TwitterConnection;
import nl.wisdelft.twitter.gatherer.TwitterConnectionManager;
import nl.wisdelft.twitter.gatherer.TwitterConnectionManager.ConnectionMode;

/**
 * @author oosterman
 */
public abstract class InputConfiguration {
	public long[] twitterUserIDs;
	public TwitterConnectionManager connectionManager;
	public TwitterConnection connection;
	public ConnectionMode connectionMode;
	
}
