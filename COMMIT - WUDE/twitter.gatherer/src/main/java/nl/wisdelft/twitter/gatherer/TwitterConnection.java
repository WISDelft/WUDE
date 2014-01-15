/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import twitter4j.Twitter;

/**
 * @author oosterman
 */
public class TwitterConnection {
	public String name;
	public Twitter connection;

	public TwitterConnection(String name, Twitter connection){
		this.name = name;
		this.connection = connection;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof TwitterConnection)) return false;
		TwitterConnection o = (TwitterConnection) other;
		if (this.name == null || o.name == null) return false;
		return this.name.equals(o.name);
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
}
