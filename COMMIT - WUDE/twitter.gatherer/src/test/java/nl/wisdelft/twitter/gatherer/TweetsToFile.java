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
public class TweetsToFile {

	AppConfiguration appConfig;
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
		// create appconfig
		appConfig = new AppConfiguration();
		appConfig.getTweets = true;
		appConfig.toFile = true;
		appConfig.fileDir = TestConstants.outputDir;

		// datepath
		path = TestConstants.outputDir + TimelineGathererThread.getOutputDirSuffix() + "/";
	}

	@Test
	public void noAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] {};
		App app = new App(appConfig);
		app.startApplication();
	}

	@Test
	public void publicAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.publicAccount };
		App app = new App(appConfig);
		app.startApplication();
		String dir = path + TestConstants.publicAccount + ".json";
		File file = new File(dir);
		Assert.assertTrue(file.exists());
	}

	@Test
	public void noFollowers() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.noFollowers };
		App app = new App(appConfig);
		app.startApplication();
		String dir = path + TestConstants.noFollowers + ".json";
		File file = new File(dir);
		Assert.assertTrue(file.exists());
	}

	@Test
	public void multipleAccounts() throws IOException, TwitterException {
		appConfig.ids = TestConstants.multipleAccounts;
		App app = new App(appConfig);
		app.startApplication();
		File file = new File(path);
		Assert.assertTrue(file.listFiles().length == TestConstants.multipleAccounts.length);
	}

	@Test
	public void privateAccount() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.privateAccount };
		App app = new App(appConfig);
		app.startApplication();
		String dir = path + TestConstants.privateAccount + ".json";
		File file = new File(dir);
		Assert.assertTrue(file.exists());
	}
	
	@Test
	public void manyTweets() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.manyTweets };
		App app = new App(appConfig);
		app.startApplication();
		String dir = path + TestConstants.manyTweets + ".json";
		File file = new File(dir);
		Assert.assertTrue(file.exists());
	}

}
