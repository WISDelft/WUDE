/**
 * 
 */
package nl.wisdelft.data;

import java.util.Date;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * @author oosterman
 */
public class JSONStatus implements Status {

	private static final long serialVersionUID = 1L;
	private Status tweet;
	private String json;

	public JSONStatus(Status tweet, String rawJSON) {
		this.tweet = tweet;
		this.json = rawJSON;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Status o) {
		return tweet.compareTo(o);
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.TwitterResponse#getRateLimitStatus()
	 */
	public RateLimitStatus getRateLimitStatus() {
		return tweet.getRateLimitStatus();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.TwitterResponse#getAccessLevel()
	 */
	public int getAccessLevel() {
		return tweet.getAccessLevel();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.EntitySupport#getUserMentionEntities()
	 */
	public UserMentionEntity[] getUserMentionEntities() {
		return tweet.getUserMentionEntities();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.EntitySupport#getURLEntities()
	 */
	public URLEntity[] getURLEntities() {
		return tweet.getURLEntities();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.EntitySupport#getHashtagEntities()
	 */
	public HashtagEntity[] getHashtagEntities() {
		return tweet.getHashtagEntities();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.EntitySupport#getMediaEntities()
	 */
	public MediaEntity[] getMediaEntities() {
		return tweet.getMediaEntities();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getCreatedAt()
	 */
	public Date getCreatedAt() {
		return tweet.getCreatedAt();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getId()
	 */
	public long getId() {
		return tweet.getId();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getText()
	 */
	public String getText() {
		return tweet.getText();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getSource()
	 */
	public String getSource() {
		return tweet.getSource();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#isTruncated()
	 */
	public boolean isTruncated() {
		return tweet.isTruncated();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getInReplyToStatusId()
	 */
	public long getInReplyToStatusId() {
		return tweet.getInReplyToStatusId();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getInReplyToUserId()
	 */
	public long getInReplyToUserId() {
		return tweet.getInReplyToUserId();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getInReplyToScreenName()
	 */
	public String getInReplyToScreenName() {
		return tweet.getInReplyToScreenName();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getGeoLocation()
	 */
	public GeoLocation getGeoLocation() {
		return tweet.getGeoLocation();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getPlace()
	 */
	public Place getPlace() {
		return tweet.getPlace();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#isFavorited()
	 */
	public boolean isFavorited() {
		return tweet.isFavorited();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getUser()
	 */
	public User getUser() {
		return tweet.getUser();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#isRetweet()
	 */
	public boolean isRetweet() {
		return tweet.isRetweet();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getRetweetedStatus()
	 */
	public Status getRetweetedStatus() {
		return tweet.getRetweetedStatus();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getContributors()
	 */
	public long[] getContributors() {
		return tweet.getContributors();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getRetweetCount()
	 */
	public long getRetweetCount() {
		return tweet.getRetweetCount();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#isRetweetedByMe()
	 */
	public boolean isRetweetedByMe() {
		return tweet.isRetweetedByMe();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#getCurrentUserRetweetId()
	 */
	public long getCurrentUserRetweetId() {
		return tweet.getCurrentUserRetweetId();
	}

	/*
	 * (non-Javadoc)
	 * @see twitter4j.Status#isPossiblySensitive()
	 */
	public boolean isPossiblySensitive() {
		return tweet.isPossiblySensitive();
	}

	/**
	 * The raw JSON from the Twitter response
	 * 
	 * @return JSON as String
	 */
	public String getJSON() {
		return json;
	}

	@Override
	public int hashCode() {
		return new Long(tweet.getId()).hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof Status){
			return this.getId() == ((Status)o).getId();
		}
		else
			return false; 
	}

	/* (non-Javadoc)
	 * @see twitter4j.Status#isRetweeted()
	 */
	public boolean isRetweeted() {
		return tweet.isRetweeted();
	}

	/* (non-Javadoc)
	 * @see twitter4j.Status#getFavoriteCount()
	 */
	public long getFavoriteCount() {
		return tweet.getFavoriteCount();
	}

	/* (non-Javadoc)
	 * @see twitter4j.Status#getIsoLanguageCode()
	 */
	public String getIsoLanguageCode() {
		return tweet.getIsoLanguageCode();
	}
}
