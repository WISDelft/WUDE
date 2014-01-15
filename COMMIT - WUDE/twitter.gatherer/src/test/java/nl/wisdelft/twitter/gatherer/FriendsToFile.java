/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
public class FriendsToFile {

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
		appConfig.getFriends = true;
		appConfig.toFile = true;
		appConfig.fileDir = TestConstants.outputDir;

		// datepath
		Calendar c = new GregorianCalendar();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String datePath = format.format(c.getTime());
		path = TestConstants.outputDir + FriendGathererThread.getOutputDirSuffix() + "/" + datePath + "/";
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
	public void noFriends() throws IOException, TwitterException {
		appConfig.ids = new long[] { TestConstants.noFriends };
		App app = new App(appConfig);
		app.startApplication();
		String dir = path + TestConstants.noFriends + ".json";
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

}
