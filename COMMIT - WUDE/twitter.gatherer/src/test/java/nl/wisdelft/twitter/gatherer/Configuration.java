/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.IOException;
import org.junit.Test;
import twitter4j.TwitterException;

/**
 * @author oosterman
 */
public class Configuration {

	@Test
	public void correctPropertiesFile() throws IOException, TwitterException {
		TwitterConnectionManager.getInstance("twittercorrect.properties");
	}

	@Test(expected = TwitterException.class)
	public void wrongPropertiesFile() throws IOException, TwitterException {
		TwitterConnectionManager.getInstance("twitterwrong.properties");
	}

	@Test(expected = IOException.class)
	public void missingPropertiesFile() throws IOException, TwitterException {
		TwitterConnectionManager.getInstance("missing.properties");
	}
}
