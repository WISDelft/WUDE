/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import nl.wisdelft.twitter.gatherer.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import twitter4j.TwitterException;

/**
 * @author oosterman
 */
public class ZipFiles {
	String path;

	@After
	public void teardown() {
		File dir = new File(path);
		FileUtils.deleteQuietly(dir);
	}

	@Before
	public void setup() {
		// create test directory
		File dir = new File(TestConstants.outputDir);
		dir.mkdirs();
		// datepath
		path = TestConstants.outputDir + TimelineGathererThread.getOutputDirSuffix() + "/";
	}

	@Test
	public void zipSmaller() throws IOException, TwitterException {
		// write non zipped
		AppConfiguration appConfig = new AppConfiguration();
		appConfig.getTweets = true;
		appConfig.toFile = true;
		appConfig.fileDir = TestConstants.outputDir;
		appConfig.zipFiles = false;
		appConfig.ids = new long[] { TestConstants.manyTweets };
		App app = new App(appConfig);
		app.startApplication();

		// write zipped
		AppConfiguration appConfig2 = new AppConfiguration();
		appConfig2.getTweets = true;
		appConfig2.toFile = true;
		appConfig2.fileDir = TestConstants.outputDir;
		appConfig2.zipFiles = true;
		appConfig2.ids = new long[] { TestConstants.manyTweets };
		App app2 = new App(appConfig2);
		app2.startApplication();

		// get the size of the files
		String unzipped = path + TestConstants.manyTweets + ".json";
		String zipped = path + TestConstants.manyTweets + ".json.zip";
		File funzipped = new File(unzipped);
		File fzipped = new File(zipped);
		long unzippedLength = funzipped.length();
		long zippedLength = fzipped.length();
		Assert.assertTrue(unzippedLength > zippedLength);
	}
	
}
