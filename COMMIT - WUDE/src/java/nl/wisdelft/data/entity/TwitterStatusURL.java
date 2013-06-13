/**
 * 
 */
package nl.wisdelft.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author oosterman
 *
 */
@Entity
@Table(name="twitterstatusurl")
public class TwitterStatusURL extends PersistentEntityManualId {
	private TwitterStatus status;
	private String originalURL;
	private String unshortenedURL;
	
	protected TwitterStatusURL(){}
	
	public TwitterStatusURL(TwitterStatus status, String originalURL, String unshorthenedURL){
		setStatus(status);
		setOriginalURL(originalURL);
		setUnshortenedURL(unshorthenedURL);
	}

	@ManyToOne
	public TwitterStatus getStatus() {
		return status;
	}
	public void setStatus(TwitterStatus status) {
		this.status = status;
	}
	@Column(name="originalurl")
	public String getOriginalURL() {
		return originalURL;
	}
	public void setOriginalURL(String originalURL) {
		this.originalURL = originalURL;
	}
	@Column(name="unshortenedurl")
	public String getUnshortenedURL() {
		return unshortenedURL;
	}
	public void setUnshortenedURL(String unshortenedURL) {
		this.unshortenedURL = unshortenedURL;
	}
}
