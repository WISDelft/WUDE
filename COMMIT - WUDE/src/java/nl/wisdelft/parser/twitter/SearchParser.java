package nl.wisdelft.parser.twitter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import nl.wisdelft.data.HibernateUtil;
import nl.wisdelft.data.Twitter4J;
import nl.wisdelft.data.entity.TwitterQuery;
import nl.wisdelft.data.entity.TwitterStatus;
import nl.wisdelft.data.repository.BaseRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class SearchParser {

	private Twitter twitter = null;
	private int tweetsPerPage = 100;
	private int pagesPerQuery = 10;
	private BaseRepository<TwitterQuery> tqRepo = new BaseRepository<TwitterQuery>(TwitterQuery.class);

	public SearchParser() throws TwitterException, IOException {
		// get a twitter connection
		twitter = Twitter4J.getInstance().getTwitterInstance();
	}

	public TwitterQuery search(TwitterQuery query) {
		// create twitter query
		Query q = new Query();
		q.setCount(tweetsPerPage);
		q.setQuery(query.getQuery());
		// query twitter for first results
		Set<TwitterStatus> statusses = new HashSet<TwitterStatus>();
		boolean performQuery = true;
		//get next pages when they are available
		int page = 0;
		//while there are next pages and we want them
		while (performQuery && page < pagesPerQuery) {
			boolean success = false;
			//redo the query when we get a rcoverable twitterexception
			while (!success) {
				try {
					QueryResult result = twitter.search(q);
					//add results
					statusses.addAll(Twitter4J.toTwitterStatus(result.getTweets()));
					if(result.hasNext()){
						q= result.nextQuery();
					}
					else{
						performQuery = false;
					}
					success = true;
				}
				catch (TwitterException ex) {
					success = handleTwitterException(ex);
				}
			}
			//increase the number of pages we have parsed
			page++;
		}
		//add the retrieved statusses to the query
		query.setStatusses(statusses);
		//store the query and the associated statusses
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction transaction = session.beginTransaction();
		tqRepo.saveOrUpdate(query);
		transaction.commit();
		return query;
	}

	/**
	 * Determines whether a twitterexception can be seen a success and sleeps the
	 * thread or changed connection when necessary
	 * 
	 * @param ex The exception
	 * @return whether the exception can be seen as a success
	 */
	private boolean handleTwitterException(TwitterException ex) {
		// handle rate limit exceptions
		if (ex.exceededRateLimitation()) {
			try {
				if (ex.getRateLimitStatus().getSecondsUntilReset() < 20) {
					// if there is less then 60 seconds left just wait
					System.out.print("Rate Limit! We wait " + ex.getRateLimitStatus().getSecondsUntilReset() + " seconds.");
					Thread.sleep(1000 * (ex.getRateLimitStatus().getSecondsUntilReset() + 1));
				}
				else {
					// else change connection and wait
					System.out.print("Rate Limit! Getting new connection...");
					twitter = Twitter4J.getInstance().getTwitterInstance(twitter);
					System.out.println("New connection: " + twitter.getId());
					Thread.sleep(1000 * 10);
				}
			}
			catch (Exception ex2) {
				ex2.printStackTrace();
			}
			// request failed
			return false;
		}
		// the user does not exist or has a private profile
		else if (ex.getStatusCode() == 404 || ex.getStatusCode() == 401) {
			// the best we can get for this request --> request success
			return true;
		}
		// twitter servers under heavy load
		else if (ex.getStatusCode() == 503) {
			// request failed
			return false;
		}
		// network error or twitter internal server error
		else if (ex.isCausedByNetworkIssue() || ex.getStatusCode() == 500) {
			// request failed
			return false;
		}
		else {
			ex.printStackTrace();
			return false;
		}
	}
}