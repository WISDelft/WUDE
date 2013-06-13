/**
 * 
 */
package nl.wisdelft.data.repository;

import java.util.Date;
import nl.wisdelft.data.entity.TwitterUser;
import nl.wisdelft.data.entity.TwitterUserRelation;
import nl.wisdelft.data.entity.TwitterUserRelation.TwitterUserRelationType;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * @author oosterman
 *
 */
public class TwitterUserRelationRepo extends AbstractRepository<TwitterUserRelation> {

	/**
	 * @param type
	 */
	public TwitterUserRelationRepo() {
		super(TwitterUserRelation.class);
	}
	
	@Override
	public TwitterUserRelation createIfNotExists(TwitterUserRelation relation){
		return createIfNotExists(relation.getUserFrom(), relation.getUserTo(), relation.getRelationType(), relation.getDate());
	}

	private TwitterUserRelation createIfNotExists(TwitterUser userFrom, TwitterUser userTo, TwitterUserRelationType relationType, Date date){
		Criteria c = super.getFactory().getCurrentSession().createCriteria(TwitterUserRelation.class);
		c.add(Restrictions.eq("userFrom" , userFrom));
		c.add(Restrictions.eq("userTo", userTo));
		c.add(Restrictions.eq("relationType", relationType));
		c.add(Restrictions.eqOrIsNull("date", date));
		TwitterUserRelation relation = (TwitterUserRelation)c.uniqueResult();
		if(relation == null){
			relation = new TwitterUserRelation(userFrom, userTo, relationType, date);
			persist(relation);
		}
			
		return relation;
	}

}
