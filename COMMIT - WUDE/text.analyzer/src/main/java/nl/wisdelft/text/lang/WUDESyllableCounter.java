/**
 * 
 */
package nl.wisdelft.text.lang;

import nl.wisdelft.text.OpenNLP.Language;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.hyphenation.Hyphenation;
import org.apache.fop.hyphenation.Hyphenator;
import de.drni.readability.phantom.analysis.SyllableCounter;
import de.drni.readability.phantom.analysis.SyllableCounterPort;

/**
 * Language dependent syllable counter. Currently supports Dutch and English.
 * 
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

		private FopFactory factory = FopFactory.newInstance();

		/**
		 * 
		 */
		public DutchSyllableCounter() {

		}

		/*
		 * (non-Javadoc)
		 * @see
		 * de.drni.readability.phantom.analysis.SyllableCounter#countSyllables(java
		 * .lang.String)
		 */
		public int countSyllables(String word) {
			Hyphenation h = Hyphenator.hyphenate("nl", null, factory.getHyphenationTreeResolver(), null, word, 2, 1);
			//no hyphens, meaning 1 syllable
			if(h==null)
				return 1;
			return h.length()+1;
		}

	}

}
