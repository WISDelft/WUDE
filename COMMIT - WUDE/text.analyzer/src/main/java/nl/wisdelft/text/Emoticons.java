/**
 * 
 */
package nl.wisdelft.text;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import opennlp.tools.util.InvalidFormatException;

/**
 * @author oosterman
 */
public class Emoticons {
	/**
	 * Top 100 Emoticons compiled from
	 * http://datagenetics.com/blog/october52012/index.html
	 */
	private static String[] all_emoticons = new String[] { ":)", ":D", ":(", ";)", ":-)", ":P", "=)", "(:", ";-)", ":/", "XD", "=D", ":o",
			"=]", "D:", ";D", ":]", ":-(", "=/", "=(", "):", "=P", ":'(", ":|", ":-D", "^_^", "(8", ":-/", ":o)", "o:", ":-P", "(;", ";P", ";]",
			":@", "=[", ":\\", ";(", ":[", "=o", "8)", ";o)", "=\\", "(=", "[:", ";O", ";/", "8D", ":}", "\\m/", ":-O", "/:", "^-^", "8-)", "=|",
			"]:", "D;", ":o(", "|:", ";-P", ");", ";-D", ":-\\", "(^_^)", "D=", "(^_^;)", ";-(", ";@", "P:", "@:", ":-|", "[=", "(^-^)", "[8",
			"(T_T)", "(-_-)", "(-:", ")=", ":{", "=}", "o;", "[;", ":?", "8-]", ":*(", "D8", ";}", ";[", ":o/", ":oP", ":-]", ":oD", "8/", "8(",
			"o(^-^)o", "Do:", "{:", ":,(", "(*^^*)", "(*^_^*)" };
	/**
	 * Positive emoticons manually assessed from the top 100
	 */
	private static String[] pos_emoticons = new String[] { ":)", ":D", ";)", ":-)", ":P", "=)", "(:", ";-)", "XD", "=D", "=]", "D:", ";D",
			":]", "=P", ":-D", "^_^", "(8", ":-P", "(;", ";P", ";]", "8)", "(=", "[:", "^-^", "8-)", "(-:", "[;", "8-]", ";}", ":-]", "{:", };
	/**
	 * Negative emoticons manually assessed from the top 100
	 */
	private static String[] neg_emoticons = new String[] { ":(", ":/", ":-(", "=/", "=(", "):", ":'(", "=[", ":[", ":{", ";[", "8(", };

	private static Set<String> emoticons = new HashSet<String>(Arrays.asList(all_emoticons));
	private static Set<String> positive_emoticons = new HashSet<String>(Arrays.asList(pos_emoticons));
	private static Set<String> negative_emoticons = new HashSet<String>(Arrays.asList(neg_emoticons));

	private Emoticons() {}

	public static boolean isEmoticon(String word) {
		return emoticons.contains(word);
	}

	public static boolean isPositiveEmoticon(String word) {
		return positive_emoticons.contains(word);
	}

	public static boolean isNegativeEmoticon(String word) {
		return negative_emoticons.contains(word);
	}

	public static Set<String> getEmoticons() {
		return emoticons;
	}

	private static int countEmoticonsInternal(String text, Set<String> emoticons) {
		OpenNLP openNLP = null;
		try {
			openNLP = new OpenNLP();
		}
		catch (InvalidFormatException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		Iterable<String> tokens = Arrays.asList(openNLP.tokenizeWhitespace(text));
		return countEmoticonsInternal(tokens, emoticons);
	}

	private static int countEmoticonsInternal(Iterable<String> tokens, Set<String> emoticons) {
		int count = 0;
		for (String token : tokens) {
			if (emoticons.contains(token)) count++;
		}
		return count;
	}

	public static int countPositiveEmoticons(Iterable<String> text) {
		return countEmoticonsInternal(text, positive_emoticons);
	}

	public static int countPositiveEmoticons(String text) {
		return countEmoticonsInternal(text, positive_emoticons);
	}

	public static int countNegativeEmoticons(String text) {
		return countEmoticonsInternal(text, negative_emoticons);
		
	}
	public static int countNegativeEmoticons(Iterable<String> text) {
		return countEmoticonsInternal(text, negative_emoticons);
	}

	public static int countEmoticons(String text) {
		return countEmoticonsInternal(text, emoticons);
	}
	public static int countEmoticons(Iterable<String> text) {
		return countEmoticonsInternal(text, emoticons);
	}

	

	

}
