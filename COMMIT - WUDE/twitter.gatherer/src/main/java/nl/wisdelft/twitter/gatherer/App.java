package nl.wisdelft.twitter.gatherer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import nl.wisdelft.twitter.gatherer.TwitterConnectionManager.ConnectionMode;
import nl.wisdelft.twitter.gatherer.configuration.AppConfiguration;
import nl.wisdelft.twitter.gatherer.configuration.FollowerConfiguration;
import nl.wisdelft.twitter.gatherer.configuration.FriendConfiguration;
import nl.wisdelft.twitter.gatherer.configuration.OutputConfiguration;
import nl.wisdelft.twitter.gatherer.configuration.ProfileConfiguration;
import nl.wisdelft.twitter.gatherer.configuration.TimelineConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import twitter4j.TwitterException;

/**
 * Hello world!
 */
public class App {
	private static Options cmdOptions = new Options();
	// I for input, U for user data, T for tweets, O for output
	static {
		cmdOptions.addOption("Ifile", true, "Reads in twitter IDs from the specified file");
		cmdOptions.addOption("Ilist", true, "Reads in twitter IDs from the provided argument (comma seperated list)");
		cmdOptions.addOption("Idb", false, "Reads in twitter IDs from the configured DB");
		cmdOptions.addOption("Up", false, "Gets the twitter profile of the twitter IDs");
		cmdOptions.addOption("Ufr", true,
				"Gets the friends of the twitter IDs. Parameter value (int) is the maximum number of friends gathered. ");
		cmdOptions.addOption("Ufo", true,
				"Gets the followers of the twitter IDs. Parameter value (int) is the maximum number of followers gathered.");
		cmdOptions.addOption("Ufocount", false, "Gets the friends of the twitter IDs");
		cmdOptions.addOption("T", false, "Gets the latest (at most 3200) tweets of the twitter IDs");
		cmdOptions.addOption("Ofile", false, "Outputs the data into files");
		cmdOptions.addOption("Ofiledir", true, "Output directory where files are stored.");
		cmdOptions.addOption("Ofileoverwrite", false,
				"Overwrites existing files with new gathered information. Default is to skip users which were already parsed current day.");
		cmdOptions.addOption("Ofilezip", false, "Write zipped files to disk");
		cmdOptions.addOption("Odb", false, "Outputs the data to the configured DB");
		cmdOptions.addOption("DBcreate", false,
				"Initializes the DB and creates the schema. All other options are ignored. WARNING! ALL DB CONTENT IS LOST.");
		cmdOptions.addOption("DBupdate", false, "Initializes the DB and updates the schema. All other options are ignored.");
	}

	public AppConfiguration appConfig = null;

	protected static long[] readIdsFromFile(String file) throws IOException {
		File f = new File(file);
		BufferedReader reader;
		if (f.exists()) {
			reader = new BufferedReader(new FileReader(f));
		}
		else {
			InputStream stream = ClassLoader.getSystemResourceAsStream(file);
			if (stream != null) {
				reader = new BufferedReader(new InputStreamReader(stream));
			}
			else {
				throw new FileNotFoundException("File not found: " + file);
			}
		}
		String line = null;
		List<Long> ids = new ArrayList<Long>();

		while ((line = reader.readLine()) != null) {
			long id = Long.parseLong(line);
			ids.add(id);
		}

		long[] l_ids = new long[ids.size()];
		int i = 0;
		for (Long id : ids) {
			l_ids[i] = id;
			i++;
		}
		System.out.println("Read in " + l_ids.length + " from '" + file + "'.");
		return l_ids;
	}

	public static void main(String[] args) throws IOException, TwitterException {
		CommandLineParser parser = new GnuParser();
		AppConfiguration appConfig = new AppConfiguration();

		try {
			CommandLine cmd = parser.parse(cmdOptions, args);
			if (cmd.getOptions().length == 0) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java", cmdOptions);
				return;
			}
			// check if we need to (re)create the DB
			if (cmd.hasOption("DBcreate")) {
				Configuration cfg = new Configuration();
				cfg.configure("hibernate.cfg.xml");
				new SchemaExport(cfg).create(false, true);
				return;
			}
			if (cmd.hasOption("DBupdate")) {
				Configuration cfg = new Configuration();
				cfg.configure("hibernate.cfg.xml");
				new SchemaUpdate(cfg).execute(false, true);
				return;
			}

			// Parse where the list of twitter ids come from
			if (cmd.hasOption("Ilist")) {
				String list = cmd.getOptionValue("Ilist");
				String[] s_ids = list.split(",");
				appConfig.ids = new long[s_ids.length];
				for (int i = 0; i < s_ids.length; i++) {
					long id = Long.parseLong(s_ids[i]);
					appConfig.ids[i] = id;
				}
			}
			if (cmd.hasOption("Ifile")) {
				String file = cmd.getOptionValue("Ifile");
				appConfig.ids = readIdsFromFile(file);
			}
			// Parse which non-tweet data we need to get
			if (cmd.hasOption("Up")) appConfig.getProfile = true;
			if (cmd.hasOption("Ufr")) {
				appConfig.getFriends = true;
				appConfig.friendsMaxCount = Integer.parseInt(cmd.getOptionValue("Ufr"));
			}
			if (cmd.hasOption("Ufo")) {
				appConfig.getFollowers = true;
				appConfig.followersMaxCount = Integer.parseInt(cmd.getOptionValue("Ufo"));
			}

			// Parse which tweet data we need to get
			if (cmd.hasOption("T")) {
				appConfig.getTweets = true;
			}
			// Parse how we store the resulting data
			if (cmd.hasOption("Ofile")) {
				appConfig.toFile = true;
				if (cmd.hasOption("Ofiledir")) {
					appConfig.fileDir = cmd.getOptionValue("Ofiledir");
				}
				if (cmd.hasOption("Ofilezip")) {
					appConfig.zipFiles = true;
				}
				if (cmd.hasOption("Ofileoverwrite")) {
					appConfig.fileOverwrite = true;
				}
			}
			if (cmd.hasOption("Odb")) {
				appConfig.toDB = true;
			}
		}
		catch (Exception ex) {
			System.err.println("Parsing failed.  Reason: " + ex.getMessage());
			return;
		}
		System.out.println("Starting application...");
		// Configuration is successfully parsed.
		// Start the application.
		App app = new App(appConfig);
		app.startApplication();

	}

	public App(AppConfiguration appConfig) {
		this.appConfig = appConfig;
	}

	public void startApplication() throws IOException, TwitterException {
		if (appConfig == null) {
			return;
		}
		/*
		 * We now know what to do, start the processes accordingly.
		 */
		TwitterConnectionManager connectionManager = TwitterConnectionManager.getInstance();
		long start = Calendar.getInstance().getTimeInMillis();

		// Each thread shares the same output configuration
		OutputConfiguration outputConfig = new OutputConfiguration();
		outputConfig.toDB = appConfig.toDB;
		outputConfig.toFile = appConfig.toFile;
		outputConfig.fileDirectory = appConfig.fileDir;
		outputConfig.zipFiles = appConfig.zipFiles;
		outputConfig.fileOverwrite = appConfig.fileOverwrite;

		List<Thread> threads = new ArrayList<Thread>();
		// Connections can be changed per thread, so each thread has its own
		// inputConfig
		if (appConfig.getFriends) {
			FriendConfiguration inputConfig = new FriendConfiguration();
			inputConfig.connectionMode = ConnectionMode.POOL;
			inputConfig.connectionManager = connectionManager;
			inputConfig.twitterUserIDs = appConfig.ids;
			inputConfig.maxFriends = appConfig.friendsMaxCount;
			FriendGathererThread t = new FriendGathererThread(inputConfig, outputConfig);
			t.start();
			threads.add(t);
		}
		if (appConfig.getFollowers) {
			FollowerConfiguration inputConfig = new FollowerConfiguration();
			inputConfig.connectionMode = ConnectionMode.POOL;
			inputConfig.connectionManager = connectionManager;
			inputConfig.twitterUserIDs = appConfig.ids;
			inputConfig.maxFollowers = appConfig.followersMaxCount;
			FollowerGathererThread t = new FollowerGathererThread(inputConfig, outputConfig);
			t.start();
			threads.add(t);
		}
		if (appConfig.getProfile) {
			ProfileConfiguration inputConfig = new ProfileConfiguration();
			inputConfig.connectionMode = ConnectionMode.POOL;
			inputConfig.connectionManager = connectionManager;
			inputConfig.twitterUserIDs = appConfig.ids;
			ProfileGathererThread t = new ProfileGathererThread(inputConfig, outputConfig);
			t.start();
			threads.add(t);
		}
		if (appConfig.getTweets) {
			TimelineConfiguration inputConfig = new TimelineConfiguration();
			inputConfig.connectionMode = ConnectionMode.POOL;
			inputConfig.connectionManager = connectionManager;
			inputConfig.twitterUserIDs = appConfig.ids;
			TimelineGathererThread t = new TimelineGathererThread(inputConfig, outputConfig);
			t.start();
			threads.add(t);
		}

		// wait for all the threads to die
		for (Thread t : threads) {
			try {
				t.join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		long finish = Calendar.getInstance().getTimeInMillis();
		System.out.println("Twitter gatherer completed in " + (finish - start) / 1000 + " seconds.");
	}

}
