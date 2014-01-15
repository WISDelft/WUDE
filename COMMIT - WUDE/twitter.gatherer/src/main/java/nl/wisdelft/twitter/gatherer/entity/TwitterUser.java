/**
 * 
 */
package nl.wisdelft.twitter.gatherer.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author oosterman
 */
@Entity
@Table(name = "twitteruser")
public class TwitterUser extends PersistentEntityGeneratedId {

	private long userId;
	private String name;
	private String screenName;
	private String location;
	private String description;
	private String URL;
	private int followerCount;
	private int friendsCount;
	private Date createdAt;
	private String lang;
	private String rawJSON;
	private boolean protectedAccount;

	protected TwitterUser() {}

	public TwitterUser(long userId) {
		setUserId(userId);
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "screenname")
	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	@Column(name = "location")
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "url")
	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	@Column(name = "followercount")
	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	@Column(name = "friendscount")
	public int getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(int friendsCount) {
		this.friendsCount = friendsCount;
	}

	@Column(name = "createdat")
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Column(name = "lang")
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	@Lob
	@Column(name = "rawjson")
	public String getRawJSON() {
		return rawJSON;
	}

	public void setRawJSON(String rawJSON) {
		this.rawJSON = rawJSON;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof TwitterUser)) return false;
		TwitterUser o = (TwitterUser) other;
		if (this.userId <= 0 || o.userId <= 0 || this.getDateHarvested() == null || o.getDateHarvested() == null) return false;
		return this.userId == o.userId && this.getDateHarvested().equals(o.getDateHarvested());
	}

	@Override
	public int hashCode() {
		return (this.getClass().toString() + Long.toString(this.userId) + this.getDateHarvested().toString()).hashCode();
	}

	@Column(name = "protected")
	public boolean isProtectedAccount() {
		return protectedAccount;
	}

	public void setProtectedAccount(boolean protectedAccount) {
		this.protectedAccount = protectedAccount;
	}
}
