/**
 * 
 */
package nl.wisdelft.um;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import nl.wisdelft.data.DataLayer;

/**
 * @author oosterman
 */
public class CreateUserDocuments {
	protected static final String path = "/Users/oosterman/Documents/Data/userdocuments";
	protected static BlockingQueue<Long> userIds;
	protected static boolean recreate;
	protected static Set<String> files;
	private static int nrWorkers = 3;
	protected static int minimumTweets = 100;

	public static void main(String[] args) throws FileNotFoundException {
		DataLayer datalayer = new DataLayer();
		File dir = new File(path);
		files = new HashSet<String>();
		if (dir.exists() && dir.isDirectory()) {
			// get all the files in the directory
			File[] fs = dir.listFiles();
			for (File file : fs) {
				// only get the files
				if (file.isFile()) files.add(file.getName());
			}
		}
		else {
			throw new FileNotFoundException("Directory not found: " + path);
		}

		// get the userIdsand put them into the queue
		Set<Long> ids = datalayer.getUsersWithParsedTimeline();
		userIds = new ArrayBlockingQueue<Long>(ids.size());
		userIds.addAll(ids);

		System.out.println("CreateUserDocuments system initialized at " + new Date());

		// start the processing threads
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < nrWorkers; i++) {
			WorkerThread thread = new WorkerThread();
			thread.start();
			threads.add(thread);
		}
		// wait for the workerthreads to finish
		for (Thread t : threads) {
			try {
				t.join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

final class WorkerThread extends Thread {
	

	@Override
	public void run() {
		System.out.println("WorkerThread "+Thread.currentThread().getId()+" started");
		File doc;
		BufferedWriter writer;
		DataLayer datalayer = new DataLayer();
		Long id = null;
		while ((id = CreateUserDocuments.userIds.poll()) != null) {
			// create the document
			doc = new File(CreateUserDocuments.path, id.toString());
			
			// if we dont have to recreate and the file already exists
			if (!CreateUserDocuments.recreate && doc.exists()) {
				continue;
			}
			else {
				// get the data
				Map<Long, String> tweets = datalayer.getUserTweets(id,false);
				//if there are enough tweets
				if(tweets.size()<CreateUserDocuments.minimumTweets)
					continue;
				
				UserDocument ud = new UserDocument(id, tweets);
				try {
					writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(doc, true), "UTF-8"));
					String document = ud.getDocument();
					writer.write(document);
					writer.flush();
					writer.close();
					if(CreateUserDocuments.userIds.size()%100==0)
						System.out.println("User documents to create: " + CreateUserDocuments.userIds.size() + " - " + new Date());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
