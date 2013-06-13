/**
 * 
 */
package nl.wisdelft.gatherer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.wisdelft.data.HibernateUtil;
import nl.wisdelft.data.entity.TwitterUser;
import nl.wisdelft.data.entity.TwitterUser.UserType;
import nl.wisdelft.data.repository.TwitterUserRepo;
import nl.wisdelft.parser.twitter.BasicParser;
import nl.wisdelft.parser.twitter.ListParserThread;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import twitter4j.TwitterException;

/**
 * @author oosterman
 */
public class CSExpertDataset {

	static String dirPath = ".";

	/**
	 * @param args
	 * @throws TwitterException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, TwitterException, InterruptedException {
		// store the initial experts
		insertExperts();

		// get all their friends and followers
		Transaction transaction = HibernateUtil.beginTransaction();
		List<TwitterUser> experts = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(TwitterUser.class).add(
				Restrictions.eq("userType", UserType.SEED)).list();
		transaction.commit();
		
		/*FollowerFriendParserThread ffthread = new FollowerFriendParserThread(experts);
		ffthread.start();
		ffthread.join();
		 */
		
		ListParserThread lpthread = new ListParserThread(experts);
		lpthread.parseListsOwnedByUsers = false;
		lpthread.parseListsSubscribedByUser = false;
		lpthread.parseSubscribersOfList = false;
		lpthread.start();
		lpthread.join();
	}

	private static void insertExperts() throws IOException, TwitterException {
		TwitterUserRepo tuRepo = new TwitterUserRepo();
		Long[] idsSemWeb = new Long[] { 17580853L, 46680156L, 108586742L, 140956562L, 19122108L, 52206988L, 22216174L, 114422623L, 15336340L,
				52421562L, 12551552L, 26823198L, 15376357L, 144658895L, 41572443L, 815903L };
		Long[] idsUMAP = new Long[] { 16534969L, 39980557L, 264501255L, 14048484L, 117079124L, 37908775L, 23813190L, 16489542L, 14757518L,
				27500398L, 51302089L, 105090896L, 112883228L, 15089078L, 38221421L, 151750574L, 9316452L };
		Long[] idsCrowd = new Long[] { 226005722L, 326793887L, 2519891L, 24753328L, 30339571L, 119512412L, 32337776L, 16136933L, 14757518L,
				8381682L, 333597222L, 34295510L, 1114635888L, 48380240L };
		Long[] idsWebEng = new Long[] { 14049302L, 39980557L, 552563381L, 14238872L, 37908775L, 110420616L, 817540L, 1114635888L, 25893118L,
				269210038L };

		// concatenate all the ids
		Set<Long> ids = new HashSet<Long>();
		ids.addAll(Arrays.asList(idsSemWeb));
		ids.addAll(Arrays.asList(idsUMAP));
		ids.addAll(Arrays.asList(idsCrowd));
		ids.addAll(Arrays.asList(idsWebEng));

		BasicParser parser = new BasicParser();

		// get the full user profile of the ids
		List<TwitterUser> users = new ArrayList<TwitterUser>();
		for (long id : ids) {
			TwitterUser user = parser.getUser(id);
			if (user != null) users.add(user);
		}

		// store the full users
		Transaction transaction = HibernateUtil.beginTransaction();
		for (TwitterUser user : users) {
			user.setUserType(UserType.SEED);
			tuRepo.createIfNotExists(user);
		}
		transaction.commit();
	}
}
