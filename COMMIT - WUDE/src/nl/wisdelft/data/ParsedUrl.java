/**
 * 
 */
package nl.wisdelft.data;

/**
 * @author oosterman
 *
 */
public class ParsedUrl {
	private String originalurl;
	private String expandedurl;

	public ParsedUrl(String original, String expanded) {
		this.originalurl = original;
		this.expandedurl = expanded;
	}

	public String getOriginalurl() {
		return originalurl;
	}

	public String getExpandedurl() {
		return expandedurl;
	}
}