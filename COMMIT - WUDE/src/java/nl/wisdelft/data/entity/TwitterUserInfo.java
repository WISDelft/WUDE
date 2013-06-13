/**
 * 
 */
package nl.wisdelft.data.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * @author oosterman
 */
@Entity
@Table(name = "twitteruserinfo")
public class TwitterUserInfo extends PersistentEntityManualId implements Serializable {
	private static final long serialVersionUID = 1L;
	private TwitterUser user;
	private boolean timelineParsed;
	private boolean friendsParsed;
	private boolean followersParsed;
	private boolean listsParsed;
	private RelationToSeed relationToSeed;

	protected TwitterUserInfo() {}

	public TwitterUserInfo(TwitterUser user) {
		setUser(user);
	}

	@OneToOne
	@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
	public TwitterUser getUser() {
		return user;
	}

	public void setUser(TwitterUser user) {
		this.user = user;
		setId(user.getId());
	}

	public boolean isTimelineParsed() {
		return timelineParsed;
	}

	public void setTimelineParsed(boolean timelineParsed) {
		this.timelineParsed = timelineParsed;
	}

	public boolean isFriendsParsed() {
		return friendsParsed;
	}

	public void setFriendsParsed(boolean friendsParsed) {
		this.friendsParsed = friendsParsed;
	}

	public boolean isFollowersParsed() {
		return followersParsed;
	}

	public void setFollowersParsed(boolean followersParsed) {
		this.followersParsed = followersParsed;
	}

	public boolean isListsParsed() {
		return listsParsed;
	}

	public void setListsParsed(boolean listsParsed) {
		this.listsParsed = listsParsed;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public RelationToSeed getRelationToSeed() {
		return relationToSeed;
	}

	public void setRelationToSeed(RelationToSeed relationToSeed) {
		this.relationToSeed = relationToSeed;
	}

	public enum RelationToSeed {
		SEED, MENTIONED, FOLLOWER, FRIEND, MEMBER_OF_LIST, SUBSCRIBER_TO_LIST
	}
}
