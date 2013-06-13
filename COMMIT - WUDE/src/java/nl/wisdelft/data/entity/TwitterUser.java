/**
 * 
 */
package nl.wisdelft.data.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author oosterman
 */
@Entity
@Table(name="twitteruser")
public class TwitterUser extends PersistentEntityManualId {
	public enum UserType {NON_SEED, SEED}
	
	private String name;
	private String screenName;
	private String location;
	private String description;
	private String URL;
	private int followerCount;
	private int friendsCount;
	private Date createdAt;
	private String lang;
	private TwitterUserInfo userInfo;
	private Set<TwitterListUserRelation> listRelations = new HashSet<TwitterListUserRelation>();
	private Set<TwitterList> ownedLists = new HashSet<TwitterList>();
	private UserType userType; 

	protected TwitterUser() {}

	public TwitterUser(Long id) {
		setId(id);
		userInfo = new TwitterUserInfo(this);
	}
	
	@Column(name="name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name="screenname")
	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	@Column(name="location")
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="url")
	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	@Column(name="followercount")
	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	@Column(name="friendscount")
	public int getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(int friendsCount) {
		this.friendsCount = friendsCount;
	}

	@Column(name="createdat")
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Column(name="lang")
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	@OneToOne(mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public TwitterUserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(TwitterUserInfo userInfo) {
		this.userInfo = userInfo;
	}

	@OneToMany(mappedBy="user",fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	public Set<TwitterListUserRelation> getListRelations() {
		return listRelations;
	}

	public void setListRelations(Set<TwitterListUserRelation> listRelations) {
		this.listRelations = listRelations;
	}

	@Column(name="usertype")
	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	@OneToMany(mappedBy="owner",fetch=FetchType.LAZY)
	public Set<TwitterList> getOwnedLists() {
		return ownedLists;
	}

	public void setOwnedLists(Set<TwitterList> ownedLists) {
		this.ownedLists = ownedLists;
	}

}
