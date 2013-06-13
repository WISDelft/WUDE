/**
 * 
 */
package nl.wisdelft.text.lang;

import de.drni.readability.phantom.analysis.SyllableCounter;
import de.drni.readability.phantom.analysis.SyllableCounterPort;
import nl.wisdelft.text.OpenNLP.Language;

/**
 * Language dependent syllable counter. Currently supports Dutch and English.
 * @author oosterman
 */
public class WUDESyllableCounter implements SyllableCounter {

	private SyllableCounter counter;

	public WUDESyllableCounter(Language lang) {
		switch (lang) {
			case DUTCH:
				counter = new DutchSyllableCounter();
				break;
			default:
				counter = new SyllableCounterPort();
				break;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.drni.readability.phantom.analysis.SyllableCounter#countSyllables(java
	 * .lang.String)
	 */
	public int countSyllables(String word) {
		return counter.countSyllables(word);
	}
	
	class DutchSyllableCounter implements SyllableCounter {

		/*
		 * (non-Javadoc)
		 * @see
		 * de.drni.readability.phantom.analysis.SyllableCounter#countSyllables(java
		 * .lang.String)
		 */
		public int countSyllables(String word) {
			return Dutch.getSyllableCount(word);
		}

	}

	
}
