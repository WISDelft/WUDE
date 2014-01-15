/**
 * 
 */
package nl.wisdelft.text;

import de.drni.readability.phantom.analysis.TextStats;

/**
 * @author oosterman
 *
 */
public class Stats implements TextStats {
	
	protected int numWords = 0;
	protected int numSentences = 0;
	protected int numSyllables = 0;
	protected int numCharacters = 0;
	protected int numHardWords = 0;
	protected int numLongWords = 0;
	protected int numPolySyllables = 0;
	protected int numMonoSyllables = 0;
	

	/**
	 * Default constructor intializing all values to 0.
	 */
	public Stats() {
	}
	
	public Stats(TextStats stats){
		this.numCharacters=stats.getNumCharacters();
		this.numHardWords = stats.getNumHardWords();
		this.numLongWords = stats.getNumLongWords();
		this.numMonoSyllables = stats.getNumMonoSyllables();
		this.numPolySyllables = stats.getNumPolySyllables();
		this.numSentences = stats.getNumSentences();
		this.numSyllables = stats.getNumSyllables();
		this.numWords= stats.getNumWords();
	}
	
	/**
	 * Constructor that allows/requires to set each
	 * and every value in this container.
	 */
	public Stats(int numCharacters, int numComplexWords,
			int numLongWords, int numPolySyllables,  int numMonoSyllables,
			int numSyllables,
			int numSentences, int numWords) {
		this.numCharacters = numCharacters;
		this.numHardWords = numComplexWords;
		this.numLongWords = numLongWords;
		this.numPolySyllables = numPolySyllables;
		this.numPolySyllables = numMonoSyllables;
		this.numSentences = numSentences;
		this.numSyllables = numSyllables;
		this.numWords = numWords;
	}
	public int getNumPolySyllables() {
		return numPolySyllables;
	}
	public int getNumMonoSyllables() {
		return numPolySyllables;
	}
	
	public int getNumWords() {
		return numWords;
	}
	public int getNumSentences() {
		return numSentences;
	}
	public int getNumSyllables() {
		return numSyllables;
	}
	public int getNumCharacters() {
		return numCharacters;
	}
	public int getNumHardWords() {
		return numHardWords;
	}
	public int getNumLongWords() {
		return numLongWords;
	}
	
	public String toString() {
		
		return "[" +
				"numCharacters=" + numCharacters + "," + 
				"numWords=" + numWords + "," + 
				"numSentences=" + numSentences + "," + 
				"numHardWords=" + numHardWords + "," +
				"numLongWords=" + numLongWords + "," +
				"numSyllables=" + numSyllables + "," +
				"numMonoSyllables=" + numMonoSyllables + "," +
				"numPolySyllables=" + numPolySyllables + 
				"]";
	}


}