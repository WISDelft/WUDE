/**
 * 
 */
package nl.wisdelft.text;

import java.util.Arrays;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.text.lang.WUDESyllableCounter;
import de.drni.readability.phantom.analysis.TextAnalyzer;
import de.drni.readability.phantom.analysis.TextStats;

/**
 * @author oosterman
 *
 */
public class BasicTextAnalyzer {
	
	private static OpenNLP openNLP;
	private static Language lang;
	
	public static TextStats analyze(String text,Language lang){
		TextAnalyzer analyzer = new TextAnalyzer();
		if(openNLP == null || BasicTextAnalyzer.lang!=lang)
			openNLP = new OpenNLP(lang);
		int sentences = openNLP.sentences(text).length;
		String[] tokens = openNLP.tokenize(text);
		return analyzer.analyze(Arrays.asList(tokens), sentences,new WUDESyllableCounter(lang));
	}
	
	public static TextStats analyze(String text){
		return new TextAnalyzer().analyze(text);
	}
}
