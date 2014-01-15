/**
 * 
 */
package nl.wisdelft.text.lang;

import java.util.List;
import nl.wisdelft.WUDEUtil;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

/**
 * @author oosterman
 */
public final class DetectLanguage {

	private static boolean initialized = false;

	/**
	 * Load profiles from specified directory. This method must be called once
	 * before language detection.
	 * 
	 * @param profileDirectory
	 * @throws LangDetectException
	 */
	public static void init() throws LangDetectException {
		DetectorFactory.loadProfile(WUDEUtil.getResourceFolder() + "/langprofiles/");
		initialized = true;
	}

	/**
	 * {@link http://code.google.com/p/language-detection/wiki/LanguageList}
	 * 
	 * @param text
	 * @param language
	 * @param threshold
	 * @return if the language {@see language} was detected with a certainty level
	 *         of at least {@see threshold}
	 * @throws LangDetectException
	 */
	public static boolean isOfLang(String text, String language, double threshold) throws LangDetectException {
		List<Language> langs = detectLangs(text);
		for (Language lang : langs) {
			if (lang.lang.equals(language) && lang.prob >= threshold) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Detect a single language for the given text
	 * 
	 * @param text
	 * @return the language code of the most probable language
	 * @throws LangDetectException
	 */
	public static String detect(String text) throws LangDetectException {
		if (!initialized) init();
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.detect();
	}

	/**
	 * Detects all languages with weiths for the given text
	 * 
	 * @param text
	 * @return A list of languages with weights
	 * @throws LangDetectException
	 */
	public static List<Language> detectLangs(String text) throws LangDetectException {
		if (!initialized) init();
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.getProbabilities();
	}

}
