/**
 * 
 */
package nl.wisdelft.data.repository;

import java.util.HashSet;
import java.util.Set;
import nl.wisdelft.data.entity.TwitterUser;
import nl.wisdelft.data.entity.TwitterUser.UserType;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * @author oosterman
 */
public class TwitterUserRepo extends AbstractRepository<TwitterUser> {

	/**
	 * @param factory
	 * @param type
	 */
	public TwitterUserRepo() {
		super(TwitterUser.class);
	}


	@SuppressWarnings("unchecked")
	public Set<TwitterUser> findUsersOfType(UserType type) {
		Criteria c = super.getFactory().getCurrentSession().createCriteria(TwitterUser.class).add(Restrictions.eq("userType", type));
		return new HashSet<TwitterUser>(c.list());
	}

}
