/**
 * 
 */
package nl.wisdelft.data.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author oosterman
 */
@Entity
@Table(name="twitteruserrelation")
public class TwitterUserRelation extends PersistentEntityGeneratedId {
	public enum TwitterUserRelationType {
		FOLLOWS, MENTIONS
	}

	private TwitterUser userFrom;
	private TwitterUser userTo;
	private TwitterUserRelationType relationType;
	private Date date;
	
	public TwitterUserRelation(){
		
	}
	
	public TwitterUserRelation(TwitterUser userFrom, TwitterUser userTo, TwitterUserRelationType relationType, Date date){
		setUserFrom(userFrom);
		setUserTo(userTo);
		setRelationType(relationType);
		setDate(date);
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	public TwitterUser getUserFrom() {
		return userFrom;
	}
	public void setUserFrom(TwitterUser userFrom) {
		this.userFrom = userFrom;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	public TwitterUser getUserTo() {
		return userTo;
	}
	
	public void setUserTo(TwitterUser userTo) {
		this.userTo = userTo;
	}
	
	@Column(name="relationtype")
	public TwitterUserRelationType getRelationType() {
		return relationType;
	}
	public void setRelationType(TwitterUserRelationType relationType) {
		this.relationType = relationType;
	}
	
	@Column(name="date")
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}
