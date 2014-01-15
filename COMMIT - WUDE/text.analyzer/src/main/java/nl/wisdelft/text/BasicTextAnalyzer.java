/**
 * 
 */
package nl.wisdelft.text;

import java.util.Arrays;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.text.lang.WUDESyllableCounter;
import de.drni.readability.phantom.analysis.TextAnalyzer;

/**
 * @author oosterman
 */
public class BasicTextAnalyzer {

	private static OpenNLP openNLP;
	private static Language lang;
	private static TextAnalyzer analyzer = new TextAnalyzer();
	private static WUDESyllableCounter counter;

	public static Stats analyze(String text, Language lang) {
		if (openNLP == null || BasicTextAnalyzer.lang != lang) {
			openNLP = new OpenNLP(lang);
			counter = new WUDESyllableCounter(lang);
			BasicTextAnalyzer.lang = lang;
		}
		int sentences = openNLP.sentences(text).length;
		//TODO Implement efficient language dependant tokenizer
		String[] tokens = openNLP.tokenizeWhitespace(text);// openNLP.tokenize(text);
		return new Stats(analyzer.analyze(Arrays.asList(tokens), sentences, counter));
	}

	public static Stats analyze(String text) {
		return new Stats(new TextAnalyzer().analyze(text));
	}
}
