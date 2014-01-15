/**
 * 
 */
package nl.wisdelft.text;

import java.io.IOException;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.InvalidFormatException;

/**
 * @author oosterman
 */
public class OpenNLP {
	protected final static String OPENNLP_SENTENCES_NL_FILE = "nl-sent.bin";
	protected final static String OPENNLP_TOKENIZER_NL_FILE = "nl-token.bin";
	protected final static String OPENNLP_SENTENCES_EN_FILE = "en-sent.bin";
	protected final static String OPENNLP_TOKENIZER_EN_FILE = "en-token.bin";

	protected static Tokenizer tokenizer = null;
	protected static SentenceDetector sentenceDetector = null;
	protected static Language currentLoadedLanguage;

	public enum Language {
		DUTCH, ENGLISH
	};

	public OpenNLP() throws InvalidFormatException, IOException {
		this(Language.ENGLISH);
	}

	public OpenNLP(Language lang) {
		try {
			// load tokenizer if not already loaded
			if (tokenizer == null || currentLoadedLanguage != lang) {
				TokenizerModel tokenizerModel = null;
				switch (lang) {
					case DUTCH:
						tokenizerModel = new TokenizerModel(ClassLoader.getSystemResourceAsStream(OPENNLP_TOKENIZER_NL_FILE));
						break;
					case ENGLISH:
						tokenizerModel = new TokenizerModel(ClassLoader.getSystemResourceAsStream(OPENNLP_TOKENIZER_EN_FILE));
						break;
				}
				tokenizer = new TokenizerME(tokenizerModel);
			}
			// load sentence splitter if not loaded
			if (sentenceDetector == null || currentLoadedLanguage != lang) {
				SentenceModel sentenceModel = null;
				switch (lang) {
					case DUTCH:
						sentenceModel = new SentenceModel(ClassLoader.getSystemResourceAsStream(OPENNLP_SENTENCES_NL_FILE));
						break;
					case ENGLISH:
						sentenceModel = new SentenceModel(ClassLoader.getSystemResourceAsStream(OPENNLP_SENTENCES_EN_FILE));
						break;
				}
				sentenceDetector = new SentenceDetectorME(sentenceModel);
			}
			OpenNLP.currentLoadedLanguage = lang;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Tokenizes the string using OpenNLP language specific tokenizer
	 * 
	 * @param text
	 * @return
	 */
	public String[] tokenize(String text) {
		return tokenizer.tokenize(text);
	}

	/**
	 * Tokenizes the string using OpenNLP language specific tokenizer
	 * 
	 * @param text
	 * @return
	 */
	public String[] sentences(String text) {
		return sentenceDetector.sentDetect(text);
	}

	/**
	 * Tokenizes the string using the OpenNLP whitespace tokenizer
	 * 
	 * @param text
	 * @return
	 */
	public String[] tokenizeWhitespace(String text) {
		return WhitespaceTokenizer.INSTANCE.tokenize(text);
	}

}
