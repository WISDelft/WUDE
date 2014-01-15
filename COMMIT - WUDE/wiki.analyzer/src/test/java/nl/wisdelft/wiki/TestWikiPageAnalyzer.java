/**
 * 
 */
package nl.wisdelft.wiki;

import nl.wisdelft.wiki.analysis.WikiPageAnalyzer;
import junit.framework.Assert;

/**
 * @author oosterman
 *
 */
public class TestWikiPageAnalyzer {

	public void TestSpecialPage(){
		Assert.assertFalse(WikiPageAnalyzer.isSpecialPage(null));
		Assert.assertFalse(WikiPageAnalyzer.isSpecialPage(""));
		Assert.assertFalse(WikiPageAnalyzer.isSpecialPage("Overleg test"));
		Assert.assertTrue(WikiPageAnalyzer.isSpecialPage("Overleg: test"));
		Assert.assertTrue(WikiPageAnalyzer.isSpecialPage("Overleg:"));		
	}
}
