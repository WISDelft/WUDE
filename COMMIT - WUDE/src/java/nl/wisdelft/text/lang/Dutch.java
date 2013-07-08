/**
 * 
 */
package nl.wisdelft.text.lang;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import nl.wisdelft.WUDEUtil;
import dk.dren.hunspell.Hunspell;

/**
 * Uses https://github.com/dren-dk/HunspellJNA Uses
 * https://github.com/dedeibel/libhyphenjna http://xmlgraphics.apache.org/fop/
 * http://offo.sourceforge.net/
 * 
 * @author oosterman
 */
public class Dutch {

	private static String DICTIONARY_FILES_NL = WUDEUtil.getResourceFilePath("nl_NL");
	
	
	public static dk.dren.hunspell.Hunspell.Dictionary getSpellingDictionary() {
		try {
			return Hunspell.getInstance().getDictionary(DICTIONARY_FILES_NL);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
		catch (UnsupportedOperationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
