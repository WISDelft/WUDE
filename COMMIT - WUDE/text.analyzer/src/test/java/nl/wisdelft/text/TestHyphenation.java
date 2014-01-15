/**
 * 
 */
package nl.wisdelft.text;

import junit.framework.Assert;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.text.lang.WUDESyllableCounter;
import org.junit.Test;

/**
 * @author oosterman
 *
 */
public class TestHyphenation {
	String dutch = "schoenmakersgereedschap";
	String english = "housekeepermoney";
	
	@Test
	public void TestDutch(){
		WUDESyllableCounter counter = new WUDESyllableCounter(Language.DUTCH);
		int count = counter.countSyllables(dutch);
		Assert.assertEquals(count, 6);
	}
	
	@Test
	public void TestEnglish(){
		WUDESyllableCounter counter = new WUDESyllableCounter(Language.ENGLISH);
		int count = counter.countSyllables(english);
		Assert.assertEquals(count, 5);
	}

}
