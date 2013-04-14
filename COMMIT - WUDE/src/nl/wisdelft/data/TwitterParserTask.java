package nl.wisdelft.data;

public class TwitterParserTask {
	private int id;
	private String searchString = null;
	private Float longitude = null;
	private Float latitude = null;
	private Integer radius = null;
	private String language = null;
	private Integer maxTweets = null;
	private boolean isSearch = false;
	private boolean isStream = false;
	private boolean addFoundUsers = false;
	private boolean isActive = true;
	private boolean isQueried = false;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Float getLongitude() {
		return longitude;
	}

	public void setLongitude(Float longitude) {
		if (longitude == 0) this.longitude = null;
		else this.longitude = longitude;

	}

	public Float getLatitude() {
		return latitude;
	}

	public void setLatitude(Float latitude) {
		if (latitude == 0) this.latitude = null;
		else this.latitude = latitude;
	}

	public Integer getRadius() {
		return radius;
	}

	public void setRadius(Integer radius) {
		this.radius = radius;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Integer getMaxTweets() {
		return maxTweets;
	}

	public void setMaxTweets(Integer maxTweets) {
		if (maxTweets == 0) this.maxTweets = null;
		else this.maxTweets = maxTweets;
	}

	public boolean isSearch() {
		return isSearch;
	}

	public void setSearch(boolean isSearch) {
		this.isSearch = isSearch;
	}

	public boolean isStream() {
		return isStream;
	}

	public void setStream(boolean isStream) {
		this.isStream = isStream;
	}

	public boolean isAddFoundUsers() {
		return addFoundUsers;
	}

	public void setAddFoundUsers(boolean addFoundUsers) {
		this.addFoundUsers = addFoundUsers;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isQueried() {
		return isQueried;
	}

	public void setQueried(boolean isQueried) {
		this.isQueried = isQueried;
	}

	@Override
	public boolean equals(Object other){
		if(other instanceof TwitterParserTask){
			TwitterParserTask o = (TwitterParserTask)other;
			return this.getId() == o.getId();
		}
		else 
			return false;
	}
	
	@Override
	public int hashCode(){
		return new Integer(this.getId()).hashCode();
	}
	
}