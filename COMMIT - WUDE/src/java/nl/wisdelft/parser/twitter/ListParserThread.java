package nl.wisdelft.parser.twitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import nl.wisdelft.data.HibernateUtil;
import nl.wisdelft.data.Twitter4J;
import nl.wisdelft.data.entity.TwitterList;
import nl.wisdelft.data.entity.TwitterListUserRelation;
import nl.wisdelft.data.entity.TwitterListUserRelation.TwitterListRelationType;
import nl.wisdelft.data.entity.TwitterUser;
import nl.wisdelft.data.repository.BaseRepository;
import nl.wisdelft.data.repository.TwitterListRepo;
import nl.wisdelft.data.repository.TwitterUserRepo;
import org.hibernate.Session;
import org.hibernate.Transaction;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

/**
 * Threaded class to retrieve Lists from twitter. Based on the parse* variables
 * certain relations are stored
 * 
 * @author oosterman
 */
public class ListParserThread extends Thread {
	private Queue<TwitterUser> users = null;
	public boolean parseListsOwnedByUsers = true;
	public boolean parseListsUserIsMember = true;
	public boolean parseListsSubscribedByUser = true;
	public boolean parseSubscribersOfList = true;
	public boolean parseMembersOfList = true;
	private Twitter twitter = null;
	private TwitterUserRepo tuRepo = new TwitterUserRepo();
	private TwitterListRepo tlRepo = new TwitterListRepo();
	private BaseRepository<TwitterListUserRelation> tluRepo = new BaseRepository<TwitterListUserRelation>(TwitterListUserRelation.class);
	private Twitter4J twitter4j = null;

	public ListParserThread(List<TwitterUser> users) throws IOException, TwitterException {
		twitter4j = Twitter4J.getInstance();
		this.users =new LinkedList<TwitterUser>(users);
		twitter = twitter4j.getTwitterInstance();
	}

	@Override
	public void run() {
		System.out.println("ListParser thread started");
		boolean success;

		// for each user determine the lists it has
		while (!users.isEmpty()) {
			TwitterUser twUser = users.remove();
			// check whether we already have parsed the lists
			if (twUser.getUserInfo().isListsParsed()) continue;

			// if lists owned and subscribed to by the users need to be parsed
			if (parseListsOwnedByUsers || parseListsSubscribedByUser) {
				Queue<UserList> lists = null;
				success = false;
				while (!success) {
					try {
						lists = new LinkedList<UserList>(twitter.getUserLists(twUser.getId()));
						success = true;
					}
					catch (TwitterException ex) {
						success = handleTwitterException(ex);
					}
				}
				while (!lists.isEmpty()) {
					UserList list = lists.remove();
					// check if the list is owned or subscribed to
					if ((list.getUser().getId() == twUser.getId() && parseListsOwnedByUsers)
							|| (list.getUser().getId() != twUser.getId() && parseListsSubscribedByUser)) {
						storeList(list);
					}
				}
			}
			// if we need to parse the lists where the user is in
			if (parseListsUserIsMember) {
				PagableResponseList<UserList> lists = null;
				success = false;
				while (!success) {
					try {
						while (lists == null || lists.hasNext()) {
							if (lists == null) lists = twitter.getUserListMemberships(twUser.getId(), -1);
							else {
								PagableResponseList<UserList> temp = twitter.getUserListMemberships(twUser.getId(), lists.getNextCursor());
								temp.addAll(lists);
								lists = temp;
							}
							success = true;
						}
					}
					catch (TwitterException ex) {
						success = handleTwitterException(ex);
					}
				}
				// transform into a queue for garbage collection
				Queue<UserList> q_lists = new LinkedList<UserList>(lists);
				while (!q_lists.isEmpty()) {
					storeList(q_lists.remove());
				}
			}
			System.out.println("ListParserThread - Parsed lists of user @" + twUser.getScreenName());
			// update the user that his lists has been parsed
			Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			Transaction transaction = session.beginTransaction();
			// update user
			twUser.getUserInfo().setListsParsed(true);
			tuRepo.update(twUser);
			// save
			transaction.commit();
			// allow the user to be garbage collected
			twUser = null;
		}
		System.out.println("ListParserThread - Done");
	}

	public void storeList(UserList list) {
		// variables
		PagableResponseList<User> pages = null;
		boolean success = false;
		List<User> members = new ArrayList<User>();
		List<User> subscribers = new ArrayList<User>();

		// check whether the list already exists and the data was already parsed
		Transaction transaction = HibernateUtil.beginTransaction();
		TwitterList twList = tlRepo.findById(new Long(list.getId()));
		boolean membersParsed = false;
		boolean subscribersParsed = false;
		boolean listExists = twList != null;
		if (listExists) {
			membersParsed = twList.getListInfo().isParsedMembers();
			subscribersParsed = twList.getListInfo().isParsedSubscribers();
		}
		transaction.commit();

		// get the members in the list
		if (parseMembersOfList && !membersParsed) {
			pages = null;
			success = false;
			while (!success) {
				try {
					while (pages == null || pages.hasNext()) {
						if (pages == null) pages = twitter.getUserListMembers(list.getId(), -1);
						else {
							PagableResponseList<User> temp = twitter.getUserListMembers(list.getId(), pages.getNextCursor());
							temp.addAll(pages);
							pages = temp;
						}
					}
					success = true;
				}
				catch (TwitterException ex) {
					success = handleTwitterException(ex);
				}
			}
			members = pages;
		}
		// get the subscribers to the list
		if (parseSubscribersOfList && !subscribersParsed) {
			pages = null;
			success = false;
			while (!success) {
				try {
					while (pages == null || pages.hasNext()) {
						if (pages == null) pages = twitter.getUserListSubscribers(list.getId(), -1);
						else {
							PagableResponseList<User> temp = twitter.getUserListSubscribers(list.getId(), pages.getNextCursor());
							temp.addAll(pages);
							pages = temp;
						}
					}
					success = true;
				}
				catch (TwitterException ex) {
					success = handleTwitterException(ex);
				}
			}
			subscribers = pages;
		}

		// store all the data of this list in one transaction
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		transaction = session.beginTransaction();

		if (!listExists) {
			// store the list owner and list if not exists
			TwitterUser owner = Twitter4J.toTwitterUser(list.getUser());
			
			//owner = tuRepo.createIfNotExists(owner);
			twList = Twitter4J.toTwitterList(list);
			twList.setOwner(owner);
			tlRepo.saveOrUpdate(twList);
		}
		else {
			session.saveOrUpdate(twList);
		}
		// store the members
		TwitterUser twUser;
		if (parseMembersOfList) {
			for (User user : members) {
				// make sure the user is stored
				twUser = Twitter4J.toTwitterUser(user);
				//twUser = tuRepo.createIfNotExists(twUser);
				tuRepo.saveOrUpdate(twUser);
				// add the relation
				TwitterListUserRelation relation = new TwitterListUserRelation(twList, twUser, TwitterListRelationType.MEMBER);
				tluRepo.saveOrUpdate(relation);
				// updates all sides of the relation
				twUser.getListRelations().add(relation);
				twList.getUserRelations().add(relation);
			}
			twList.getListInfo().setParsedMembers(true);
		}

		// store the subscribers
		if (parseSubscribersOfList) {
			for (User user : subscribers) {
				// make sure the user is stored
				twUser = Twitter4J.toTwitterUser(user);
				//twUser = tuRepo.createIfNotExists(twUser);
				tuRepo.saveOrUpdate(twUser);
				// add the relation
				TwitterListUserRelation relation = new TwitterListUserRelation(twList, twUser, TwitterListRelationType.SUBSCRIBER);
				tluRepo.saveOrUpdate(relation);
				// updates all sides of the relation
				twUser.getListRelations().add(relation);
				twList.getUserRelations().add(relation);
			}
			twList.getListInfo().setParsedSubscribers(true);
		}

		transaction.commit();
		System.out.println("\tListParserThread - Parsed list: " + twList.getFullName());
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
					twitter = twitter4j.getTwitterInstance(twitter);
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
