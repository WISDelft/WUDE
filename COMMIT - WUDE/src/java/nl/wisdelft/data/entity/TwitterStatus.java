 /**
 * 
 */
package nl.wisdelft.data.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author oosterman
 */
@Entity
@Table(name="twitterstatus")
public class TwitterStatus extends PersistentEntityManualId{
	private TwitterUser user;
	private Date createdAt;
	private String text;
	private Long inReplyToStatusId;
	private Long inReplyToUserId;
	private String inReplyToScreenName;
	private Double latitude;
	private Double longitude;
	private long retweetCount;
	private String isoLanguageCode;
	private String rawJSON;
	private Set<TwitterStatusURL> urls = new HashSet<TwitterStatusURL>();

	protected TwitterStatus() {}

	public TwitterStatus(Long id, TwitterUser user) {
		setId(id);
		setUser(user);
	}

	@ManyToOne
	@JoinColumn
	public TwitterUser getUser() {
		return user;
	}

	protected void setUser(TwitterUser user) {
		this.user = user;
	}

	@Column(name="createdat")
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Column(name="text")
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Column(name="inreplytostatusid")
	public Long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	public void setInReplyToStatusId(Long inReplyToStatusId) {
		this.inReplyToStatusId = inReplyToStatusId;
	}

	@Column(name="inreplytouserid")
	public Long getInReplyToUserId() {
		return inReplyToUserId;
	}

	public void setInReplyToUserId(Long inReplyToUserId) {
		this.inReplyToUserId = inReplyToUserId;
	}

	@Column(name="inreplytoscreenname")
	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}

	public void setInReplyToScreenName(String inReplyToScreenName) {
		this.inReplyToScreenName = inReplyToScreenName;
	}

	@Column(name="latitude")
	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	@Column(name="longitude")
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Column(name="retweetcount")
	public long getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(long retweetCount) {
		this.retweetCount = retweetCount;
	}

	@Column(name="isolanguagecode")
	public String getIsoLanguageCode() {
		return isoLanguageCode;
	}

	public void setIsoLanguageCode(String isoLanguageCode) {
		this.isoLanguageCode = isoLanguageCode;
	}

	@Column(name="rawjson")
	public String getRawJSON() {
		return rawJSON;
	}

	public void setRawJSON(String rawJSON) {
		this.rawJSON = rawJSON;
	}

	@OneToMany
	public Set<TwitterStatusURL> getUrls() {
		return urls;
	}

	public void setUrls(Set<TwitterStatusURL> urls) {
		this.urls = urls;
	}
}
