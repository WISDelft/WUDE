package nl.wisdelft.text;

import static org.junit.Assert.*;
import nl.wisdelft.text.lang.DetectLanguage;
import org.junit.Test;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * 
 */

/**
 * @author oosterman
 */
public class TestLanguage {
	String dutchSentence = "Dit is een zin die gaat over een auto en een huis met een paar schoenen.";
	String englishSentence = "This is a sentence about a car and a house together with a pair of shoes";

	@Test
	public void TestDutch() throws LangDetectException {
		String lang = DetectLanguage.detect(dutchSentence);
		assertEquals("nl",lang);
	}
	
	@Test
	public void TestEnglish() throws LangDetectException {
		String lang = DetectLanguage.detect(englishSentence);
		assertEquals("en",lang);
	}
}
