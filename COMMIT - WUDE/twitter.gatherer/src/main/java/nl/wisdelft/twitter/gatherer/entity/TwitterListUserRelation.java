/**
 * 
 */
package nl.wisdelft.twitter.gatherer.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author oosterman
 */
@Entity
@Table(name = "twitterlistuserrelation")
public class TwitterListUserRelation extends PersistentEntityGeneratedId implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum TwitterListRelationType {
		MEMBER, SUBSCRIBER
	}

	private TwitterList list;
	private TwitterUser user;
	private TwitterListRelationType relationType;

	protected TwitterListUserRelation() {
	}

	/**
	 * @param list
	 * @param user
	 * @param relationType
	 */
	public TwitterListUserRelation(TwitterList list, TwitterUser user, TwitterListRelationType relationType) {
		setList(list);
		setUser(user);
		setRelationType(relationType);
	}


	@ManyToOne
	public TwitterList getList() {
		return list;
	}

	public void setList(TwitterList list) {
		this.list = list;
	}

	@ManyToOne
	public TwitterUser getUser() {
		return user;
	}

	public void setUser(TwitterUser user) {
		this.user = user;
	}

	@Column(name = "relationtype")
	public TwitterListRelationType getRelationType() {
		return relationType;
	}

	public void setRelationType(TwitterListRelationType relationType) {
		this.relationType = relationType;
	}

}
