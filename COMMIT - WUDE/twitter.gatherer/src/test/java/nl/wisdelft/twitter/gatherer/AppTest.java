/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.IOException;
import nl.wisdelft.twitter.gatherer.configuration.AppConfiguration;
import org.junit.Test;
import twitter4j.TwitterException;

/**
 * @author oosterman
 */
public class AppTest {
	
	@Test
	public void createNullConfig() {
		new App(null);
	}
	
	@Test
	public void createEmptyConfig() {
		new App(new AppConfiguration());
	}
	
	@Test
	public void runNullConfig() throws IOException, TwitterException {
		App a = new App(null);
		a.startApplication();
	}
	
	@Test
	public void runEmptyConfig() throws IOException, TwitterException {
		App a = new App(new AppConfiguration());
		a.startApplication();
	}
}
