/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.File;
import java.io.IOException;
import nl.wisdelft.twitter.gatherer.configuration.AppConfiguration;
import org.junit.Test;
import twitter4j.TwitterException;

/**
 * @author oosterman
 */
public class LargeScaleTest {

	@Test
	public void largeScale() throws IOException, TwitterException {
		// create test directory
		File dir = new File(TestConstants.outputDir);
		dir.mkdirs();
		// create appconfig
		AppConfiguration appConfig = new AppConfiguration();
		appConfig.getFriends = true;
		appConfig.getFollowers = true;
		appConfig.getTweets = true;
		appConfig.getProfile = true;
		appConfig.toFile = true;
		appConfig.fileDir = TestConstants.outputDir;
		
		long[] ids = App.readIdsFromFile("ids.txt");
		appConfig.ids = ids;
		
		App app = new App(appConfig);
		app.startApplication();		

	}
}
