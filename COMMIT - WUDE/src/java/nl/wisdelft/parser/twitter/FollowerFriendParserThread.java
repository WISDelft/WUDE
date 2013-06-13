/**
 * 
 */
package nl.wisdelft.parser.twitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import nl.wisdelft.data.HibernateUtil;
import nl.wisdelft.data.Twitter4J;
import nl.wisdelft.data.entity.TwitterUser;
import nl.wisdelft.data.entity.TwitterUserRelation;
import nl.wisdelft.data.entity.TwitterUserRelation.TwitterUserRelationType;
import nl.wisdelft.data.repository.TwitterUserRelationRepo;
import nl.wisdelft.data.repository.TwitterUserRepo;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/*
 * 1 follows 2 isfollowedby 3 mentions
 */

/**
 * @author oosterman Parser to get the followers from the users indicated by the
 *         inputlist The follower relation is stored and the followers are
 *         stored as users
 */
public class FollowerFriendParserThread extends Thread {
	public boolean parseFollowers = true;
	public boolean parseFriends = true;

	private Twitter twitter = null;
	private Queue<TwitterUser> users = new LinkedList<TwitterUser>();

	public FollowerFriendParserThread(Collection<TwitterUser> users) throws TwitterException, IOException {
		this.twitter = Twitter4J.getInstance().getTwitterInstance();
		this.users.addAll(users);
	}

	/**
	 * Gets all the friends of (meaning users that are followed by) the provided user. The relation is stored as userFrom FOLLOWS userTo
	 * @param userFrom
	 * @return
	 */
	private List<TwitterUserRelation> getFriendsBulk(TwitterUser userFrom) {
		List<TwitterUserRelation> relations = new ArrayList<TwitterUserRelation>();
		Queue<Long> ids = new LinkedList<Long>();
		IDs pages = null;
		boolean success = false;
		// first get all the ids
		while (!success) {
			try {
				while (pages == null || pages.hasNext()) {
					// get paged results
					if (pages == null) pages = twitter.getFriendsIDs(userFrom.getId(), -1);
					else pages = twitter.getFriendsIDs(userFrom.getId(), pages.getNextCursor());
					// add the ids to the list
					ids.addAll(Arrays.asList(ArrayUtils.toObject(pages.getIDs())));
				}
				success = true;
			}
			catch (TwitterException ex) {
				success = handleTwitterException(ex);
			}
		}

		// transform the ids into full user objects
		List<TwitterUser> twUsers = getTwitterUsersFromId(ids);

		// create the relations
		for (TwitterUser userTo : twUsers) {
			TwitterUserRelation relation = new TwitterUserRelation(userFrom, userTo, TwitterUserRelationType.FOLLOWS, null);
			relations.add(relation);
		}
		return relations;
	}

	/**
	 * Get all the user that follow the given user. The relation is stored as userTo FOLLOWS userFrom
	 * @param userFrom
	 * @return
	 */
	private List<TwitterUserRelation> getFollowersBulk(TwitterUser userFrom) {
		List<TwitterUserRelation> relations = new ArrayList<TwitterUserRelation>();
		Queue<Long> ids = new LinkedList<Long>();
		IDs pages = null;
		boolean success = false;
		// first get all the ids
		while (!success) {
			try {
				while (pages == null || pages.hasNext()) {
					// get paged results
					if (pages == null) pages = twitter.getFollowersIDs(userFrom.getId(), -1);
					else pages = twitter.getFollowersIDs(userFrom.getId(), pages.getNextCursor());
					// add the ids to the list
					ids.addAll(Arrays.asList(ArrayUtils.toObject(pages.getIDs())));
				}
				success = true;
			}
			catch (TwitterException ex) {
				success = handleTwitterException(ex);
			}
		}
		// transform the ids into full user objects
		List<TwitterUser> twUsers = getTwitterUsersFromId(ids);

		// create the relations
		for (TwitterUser userTo : twUsers) {
			TwitterUserRelation relation = new TwitterUserRelation(userTo, userFrom, TwitterUserRelationType.FOLLOWS, null);
			relations.add(relation);
		}
		return relations;
	}

	private List<TwitterUser> getTwitterUsersFromId(Queue<Long> ids) {
		// get the users based on the ids
		List<TwitterUser> users = new ArrayList<TwitterUser>();
		boolean success = false;

		while (!ids.isEmpty()) {
			// setup batch of max 100 items
			List<Long> batch = new ArrayList<Long>();
			for (int i = 0; i < 100 && !ids.isEmpty(); i++) {
				batch.add(ids.remove());
			}
			// get the users for this batch and
			while (!success) {
				try {
					List<User> result = twitter.lookupUsers(ArrayUtils.toPrimitive(batch.toArray(new Long[] {})));
					users.addAll(Twitter4J.toTwitterUser(result));
					success = true;
				}
				catch (TwitterException ex) {
					success = handleTwitterException(ex);
				}
			}
		}
		return users;
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
					System.out.print("Rate Limit! We wait " + ex.getRateLimitStatus().getSecondsUntilReset() + " seconds.");
					Thread.sleep(1000 * (ex.getRateLimitStatus().getSecondsUntilReset() + 1));
				}
				else {
					// else change connection and wait
					System.out.print("Rate Limit! Getting new connection...");
					twitter = Twitter4J.getInstance().getTwitterInstance(twitter);
					System.out.println("New connection: " + twitter.getId());
					Thread.sleep(1000 * 30);
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

	@Override
	public void run() {
		System.out.println("FollowerFriendParser - thread started");

		// for each user determine if we have the friends and followers
		// if not, get them from twitter
		while (!users.isEmpty()) {
			TwitterUser user = users.remove();
			List<TwitterUserRelation> relations = new ArrayList<TwitterUserRelation>();
			if (parseFollowers && !user.getUserInfo().isFollowersParsed()) {
				relations.addAll(getFollowersBulk(user));
				user.getUserInfo().setFollowersParsed(true);
			}
			if (parseFriends && !user.getUserInfo().isFriendsParsed()) {
				relations.addAll(getFriendsBulk(user));
				user.getUserInfo().setFriendsParsed(true);
			}
			
			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			Transaction transaction = session.beginTransaction();
			
			//update the user
			session.saveOrUpdate(user);
			
			//if there is something to do
			if (relations.size() > 0) {
				// persist the relations	
				TwitterUserRepo tuRepo = new TwitterUserRepo();
				TwitterUserRelationRepo relRepo = new TwitterUserRelationRepo();
				for (TwitterUserRelation relation : relations) {
					// make the associated (maybe transient) users persistent
					TwitterUser from = tuRepo.get(relation.getUserFrom().getId());
					if (from == null) tuRepo.persist(relation.getUserFrom());
					else relation.setUserFrom(from);
					// Persist the user if it does not exist
					TwitterUser to = tuRepo.get(relation.getUserTo().getId());
					if (to == null) tuRepo.persist(relation.getUserTo());
					else relation.setUserTo(to);

					// persist the relation if it not exists
					relation = relRepo.createIfNotExists(relation);
				}
			}
			
			transaction.commit();
			System.out.println("FollowerFriendParser - Parsed friend and followers of user: " + user.getScreenName());

		}
		System.out.println("FollowerFriendParser -  done");

	}
}
