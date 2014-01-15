/**
 * 
 */
package nl.wisdelft.wiki.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * @author oosterman
 *
 */
public class WikiMarkupStats {
	public int h2;
	public int h3;
	public int h4;
	public int h5;
	public int h6;
	public int internalLinks;
	public int externalLinks;
	public int collectionItemLink;
	public int bold;
	public int italics;
	public int boldItalics;
	public int images;
	public int references;

	public Set<String> categories = new HashSet<String>();
	
	public int getNrFormatting(){
		return bold+italics+boldItalics;
	}
	
	public int getNrSections(){
		return h2+h3+h4+h5+h6;
	}

}
