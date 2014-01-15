/**
 * 
 */
package nl.wisdelft.wiki;

import java.io.FileNotFoundException;
import javax.xml.stream.XMLStreamException;
import junit.framework.Assert;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.wiki.entity.WikiPage;
import org.junit.Test;

/**
 * @author oosterman
 *
 */
public class TestParser {
	
	String dumpPath = "/Users/oosterman/Google Drive/WISresearch/WUDE/Cit wiki dump+logs/Dumps/06-06-2013/wiki_full_dump.xml";
	
	@Test
	public void TestDumpReading() throws FileNotFoundException, XMLStreamException{
		WikiDumpParser parser = new WikiDumpParser(dumpPath, Language.DUTCH);
		parser.skip(10000);
	}
	
	@Test
	public void TestDumpParseFirst() throws FileNotFoundException, XMLStreamException{
		WikiDumpParser parser = new WikiDumpParser(dumpPath, Language.DUTCH);
		WikiPage stats = parser.next();
		Assert.assertNotNull(stats);
	}
	
	@Test
	public void TestDumpParseAll() throws FileNotFoundException, XMLStreamException{
		WikiDumpParser parser = new WikiDumpParser(dumpPath, Language.DUTCH);
		int count = 0;
		while(parser.hasNext()){
			parser.next();
			System.out.println(++count);
		}
	}
}
