/**
 * 
 */
package nl.wisdelft.text.lang;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import name.benjaminpeter.hyphen.Dictionary;
import name.benjaminpeter.hyphen.Hyphen;
import name.benjaminpeter.hyphen.HyphenationException;
import nl.wisdelft.Utility;
import dk.dren.hunspell.Hunspell;

/**
 * Uses https://github.com/dren-dk/HunspellJNA Uses
 * https://github.com/dedeibel/libhyphenjna
 * 
 * @author oosterman
 */
public class Dutch {

	private static String DICTIONARY_FILES_NL = Utility.getResourceFilePath("nl_NL");
	private static String HYPHENATION_FILE_NL = Utility.getResourceFilePath("hyph_nl_NL.dic");

	public static Dictionary getHyphenationDictionary() {
		try {
			return Hyphen.getInstance().getDictionary(HYPHENATION_FILE_NL);
		}
		catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
		catch (UnsupportedOperationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

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

	public static int getSyllableCount(String word) {
		try {
			return getHyphenationDictionary().syllables(word).size();
		}
		catch (HyphenationException e) {
			e.printStackTrace();
			return 0;
		}
	}
}
