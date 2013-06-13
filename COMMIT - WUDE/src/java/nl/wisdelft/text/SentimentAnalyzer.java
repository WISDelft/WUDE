/**
 * 
 */
package nl.wisdelft.text;

import nl.wisdelft.text.stats.SentimentStats;

/**
 * @author oosterman
 */
public class SentimentAnalyzer {

	public static SentimentStats analyze(Iterable<String> whitespaceSeperatedTokens) {
		SentimentStats stats = new SentimentStats();

		stats.emoticons = Emoticons.countEmoticons(whitespaceSeperatedTokens);
		stats.positiveEmoticons = Emoticons.countPositiveEmoticons(whitespaceSeperatedTokens);
		stats.negativeEmoticons = Emoticons.countNegativeEmoticons(whitespaceSeperatedTokens);
		
		return stats;
	}
}

