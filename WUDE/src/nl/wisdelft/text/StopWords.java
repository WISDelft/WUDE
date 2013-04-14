/**
 * 
 */
package nl.wisdelft.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author oosterman
 */
public class StopWords {
	/**
	 * Stopword compiled from
	 * http://www.damienvanholten.com/downloads/dutch-stop-words.txt
	 * http://snowball.tartarus.org/algorithms/dutch/stop.txt
	 * http://docs.oracle.com/cd/B28359_01/text.111/b28304/astopsup.htm#i634823
	 */
	private static String[] a_stopwords = new String[] { "aan", "aangaande", "aangezien", "achter", "achterna", "af", "afgelopen", "al",
			"aldaar", "aldus", "alhoewel", "alias", "alle", "allebei", "alleen", "alles", "als", "alsnog", "altijd", "altoos", "ander", "andere",
			"anders", "anderszins", "behalve", "behoudens", "beide", "beiden", "ben", "beneden", "bent", "bepaald", "betreffende", "bij",
			"binnen", "binnenin", "boven", "bovenal", "bovendien", "bovengenoemd", "bovenstaand", "bovenvermeld", "buiten", "daar", "daarheen",
			"daarin", "daarna", "daarnet", "daarom", "daarop", "daarvanlangs", "dan", "dat", "de","den", "der", "deze", "die", "dikwijls", "dit",
			"doch", "doen", "door", "doorgaand", "dus", "echter", "een", "eens", "eer", "eerdat", "eerder", "eerlang", "eerst", "elk", "elke",
			"en", "enig", "enigszins", "enkel", "er", "erdoor", "even", "eveneens", "evenwel", "gauw", "ge", "gedurende", "geen", "gehad",
			"gekund", "geleden", "gelijk", "gemoeten", "gemogen", "geweest", "gewoon", "gewoonweg", "haar", "had", "hadden", "hare", "heb",
			"hebben", "hebt", "heeft", "hem", "hen", "het", "hier", "hierbeneden", "hierboven", "hij", "hoe", "hoewel", "hun", "hunne", "iemand",
			"iets", "ik", "ikzelf", "in", "inmiddels", "inzake", "is", "ja", "je", "jezelf", "jij", "jijzelf", "jou", "jouw", "jouwe", "juist",
			"jullie", "kan", "klaar", "kon", "konden", "krachtens", "kunnen", "kunt", "later", "liever", "maar", "mag", "me", "meer", "men",
			"met", "mezelf", "mij", "mijn", "mijnent", "mijner", "mijzelf", "misschien", "mocht", "mochten", "moest", "moesten", "moet",
			"moeten", "mogen", "na", "naar", "nadat", "net", "niet", "niets", "noch", "nog", "nogal", "nu", "of", "ofschoon", "om", "omdat",
			"omhoog", "omlaag", "omstreeks", "omtrent", "omver", "onder", "ondertussen", "ongeveer", "ons", "onszelf", "onze", "ook", "op",
			"opnieuw", "opzij", "over", "overeind", "overigens", "pas", "precies", "reeds", "rond", "rondom", "sedert", "sinds", "sindsdien",
			"slechts", "sommige", "spoedig", "steeds", "'t", "tamelijk", "te", "tegen", "tenzij", "terwijl", "thans", "tijdens", "toch", "toen",
			"toenmaals", "toenmalig", "tot", "totdat", "tussen", "u", "uit", "uitgezonderd", "uw", "vaak", "van", "vandaan", "vanuit", "vanwege",
			"veel", "veeleer", "verder", "vervolgens", "vol", "volgens", "voor", "vooraf", "vooral", "vooralsnog", "voorbij", "voordat",
			"voordezen", "voordien", "voorheen", "voorop", "vooruit", "vrij", "vroeg", "waar", "waarom", "wanneer", "want", "waren", "was",
			"wat", "we", "weer", "weg", "wegens", "wel", "weldra", "welk", "welke", "werd", "wezen", "wie", "wiens", "wier", "wij", "wijzelf",
			"wil", "worden", "wordt", "zal", "ze", "zei", "zelf", "zelfs", "zich", "zichzelf", "zij", "zijn", "zijne", "zo", "zodra", "zonder",
			"zou", "zouden", "zowat", "zulke", "zullen", "zult" };
	private static Set<String> stopwords = new HashSet<String>(Arrays.asList(a_stopwords));

	private StopWords() {}

	/**
	 * Determines if {word} is a stopword in log(1)
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isStopword(String word) {
		if (word == null) return false;
		else return stopwords.contains(word.toLowerCase());
	}

	/**
	 * Returns a bag or list of words excluding stopwords
	 * 
	 * @param sentence
	 * @param sentence
	 * @return
	 */
	public static Collection<String> removeStopwords(String sentence, boolean preserveOrder) {
		return removeStopwords(sentence, preserveOrder,null);
	}

	/**
	 * Returns a bag or list of words excluding stopwords and additional terms
	 * 
	 * @param sentence
	 * @param preserveOrder Whether to preserve the order of terms in the sentence
	 * @param additionalTerms A Set of lowercase words to also remove from sentence
	 * @return
	 */
	public static Collection<String> removeStopwords(String sentence, boolean preserveOrder, Set<String> additionalTerms) {
		return removeStopwords(sentence, preserveOrder, additionalTerms,0);
	}
	
	
	/**
	 * Returns a bag or list of words excluding stopwords and additional terms
	 * 
	 * @param sentence
	 * @param preserveOrder Whether to preserve the order of terms in the sentence
	 * @param additionalTerms A Set of lowercase words to also remove from sentence
	 * @minLength The minimum length of a term. Shorter terms are removed
	 * @return
	 */
	public static Collection<String> removeStopwords(String sentence, boolean preserveOrder, Set<String> additionalTerms,int minLength) {
		String[] words = splitWords(sentence);
		Collection<String> result = getEmptyCollection(preserveOrder); 
		
		for (String s : words) {
			String lowercaseS = s.toLowerCase();
			if (lowercaseS.length()>minLength && !result.contains(lowercaseS) && !isStopword(lowercaseS) && (additionalTerms != null && !additionalTerms.contains(lowercaseS)))
				result.add(s);
		}
		return result;
	}
	
	private static Collection<String> getEmptyCollection(boolean preserveOrder){
		Collection<String> result;
		// preserve order means returning a list, slightly slower for larger
		// sentences,
		if (preserveOrder) {
			result = new ArrayList<String>();
		}
		else {
			result = new HashSet<String>();
		}
		return result;
	}
	
	private static String[] splitWords(String sentence){
		String[] words = sentence.split("\\s+");
		return words;
	}

	public static Set<String> getStopwords() {
		return stopwords;
	}
}
