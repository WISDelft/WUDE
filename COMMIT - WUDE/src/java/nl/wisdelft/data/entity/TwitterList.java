/**
 * 
 */
package nl.wisdelft.data.entity;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author oosterman
 *
 */
@Entity
@Table(name="twitterlist")
public class TwitterList extends PersistentEntityManualId{

	private TwitterUser owner;
	private String name;
	private String fullName;
	private String slug;
	private String description;
	private int subscriberCount;
	private int memberCount;
	private URI uri;
	private Set<TwitterListUserRelation> userRelations = new HashSet<TwitterListUserRelation>();
	private TwitterListInfo listInfo;
	
	protected TwitterList(){}
	
	public TwitterList(Long id, TwitterUser owner){
		setId(id);
		setOwner(owner);
		listInfo = new TwitterListInfo(this);
	}

	@ManyToOne
	public TwitterUser getOwner() {
		return owner;
	}
	public void setOwner(TwitterUser user) {
		this.owner = user;
	}
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="fullname")
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	@Column(name="slug")
	public String getSlug() {
		return slug;
	}
	public void setSlug(String slug) {
		this.slug = slug;
	}
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Column(name="subscribercount")
	public int getSubscriberCount() {
		return subscriberCount;
	}
	public void setSubscriberCount(int subscriberCount) {
		this.subscriberCount = subscriberCount;
	}
	@Column(name="membercount")
	public int getMemberCount() {
		return memberCount;
	}
	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}
	@Column(name="uri")
	public URI getUri() {
		return uri;
	}
	public void setUri(URI uri) {
		this.uri = uri;
	}

	@OneToMany(mappedBy="list", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public Set<TwitterListUserRelation> getUserRelations() {
		return userRelations;
	}

	public void setUserRelations(Set<TwitterListUserRelation> userRelations) {
		this.userRelations = userRelations;
	}

	@OneToOne(mappedBy="list", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public TwitterListInfo getListInfo() {
		return listInfo;
	}

	public void setListInfo(TwitterListInfo listInfo) {
		this.listInfo = listInfo;
	}

	
	
}
