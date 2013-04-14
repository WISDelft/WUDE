/**
 * 
 */
package nl.wisdelft.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author oosterman
 */
public class Emoticons {
	/**
	 * Emoticons compiled from http://datagenetics.com/blog/october52012/index.html
	 */
	private static String[] a_emoticons = new String[] { ":)", ":D", ":(", ";)", ":-)", ":P", "=)", "(:", ";-)", ":/", "XD", "=D", ":o", "=]", "D:",
			";D", ":]", ":-(", "=/", "=(", "):", "=P", ":'(", ":|", ":-D", "^_^", "(8", ":-/", ":o)", "o:", ":-P", "(;", ";P", ";]", ":@", "=[",
			":\\", ";(", ":[", "=o", "8)", ";o)", "=\\", "(=", "[:", ";O", ";/", "8D", ":}", "\\m/", ":-O", "/:", "^-^", "8-)", "=|", "]:", "D;",
			":o(", "|:", ";-P", ");", ";-D", ":-\\", "(^_^)", "D=", "(^_^;)", ";-(", ";@", "P:", "@:", ":-|", "[=", "(^-^)", "[8", "(T_T)",
			"(-_-)", "(-:", ")=", ":{", "=}", "o;", "[;", ":?", "8-]", ":*(", "D8", ";}", ";[", ":o/", ":oP", ":-]", ":oD", "8/", "8(",
			"o(^-^)o", "Do:", "{:", ":,(", "(*^^*)", "(*^_^*)" };
	
	private static Set<String> emoticons = new HashSet<String>(Arrays.asList(a_emoticons));
	
	private Emoticons(){}
	
	public static boolean isEmoticon(String word){
		return emoticons.contains(word);
	}
	
	public static Set<String> getEmoticons() {
		return emoticons;
	}
	
}
