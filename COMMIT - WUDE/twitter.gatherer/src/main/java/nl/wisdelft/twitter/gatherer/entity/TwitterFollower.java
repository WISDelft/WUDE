/**
 * 
 */
package nl.wisdelft.twitter.gatherer.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author oosterman
 */
@Entity
@Table(name = "twitterfollower")
public class TwitterFollower extends PersistentEntityGeneratedId {

	private long userFromId;
	private long userToId;

	public TwitterFollower() {

	}

	public TwitterFollower(long userFrom, long userTo) {
		setUserFromId(userFrom);
		setUserToId(userTo);
	}

	@Column(name = "userfromid")
	public long getUserFromId() {
		return userFromId;
	}

	public void setUserFromId(long userFrom) {
		this.userFromId = userFrom;
	}

	@Column(name = "usertoid")
	public long getUserToId() {
		return userToId;
	}

	public void setUserToId(long userTo) {
		this.userToId = userTo;
	}
}
