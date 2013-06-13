/**
 * 
 */
package nl.wisdelft.text.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.google.common.io.Files;

/**
 * @author oosterman
 */
public final class DetectLanguage {

	final static String inputDirectory = "/Users/oosterman/Documents/Data/userdocuments";
	final static String outputDirectory = "/Users/oosterman/Documents/Data/userdocumentsNL";
	final static String languageToMatch = "nl";

	final static String LANGUAGE_PROFILE_NL_FILE = "langprofiles/nl";
	
	private static boolean initialized = false;
	private static String profileDirectory;

	public static void main(String[] args) throws IOException, LangDetectException {
		// get a list of all files in the input directory
		File inputDir = new File(inputDirectory);
		if (!inputDir.exists() || !inputDir.isDirectory()) {
			throw new FileNotFoundException("Directory '" + inputDirectory + "' does not exist");
		}
		File[] inputFiles = inputDir.listFiles();
		// loop trough all the files in the input directory.
		int i=0;
		for (File inputFile : inputFiles) {
			String content = readFile(inputFile);
			String language = detect(content);
			// if the input file is of the language to match.
			if (languageToMatch.equals(language)) {
				// copy the input file into the output directory
				File outputFile = new File(outputDirectory+"/"+inputFile.getName());
				Files.copy(inputFile, outputFile);
			}
			if (++i % 100 == 0) System.out.println("Detected " + i + "/" + inputFiles.length);
		}
		System.out.println("Done!");
	}

	private static String readFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		}
		finally {
			stream.close();
		}
	}

	/**
	 * Load profiles from specified directory. This method must be called once
	 * before language detection.
	 * 
	 * @param profileDirectory
	 * @throws LangDetectException
	 */
	public static void init(String profileDirectory) throws LangDetectException {
		DetectorFactory.loadProfile(profileDirectory);
		initialized = true;
	}

	private static void init() throws LangDetectException {
		init(profileDirectory);
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
	 * @param text
	 * @return
	 * @throws LangDetectException
	 */
	public static String detect(String text) throws LangDetectException {
		if (!initialized) init();
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.detect();
	}

	public static List<Language> detectLangs(String text) throws LangDetectException {
		if (!initialized) init();
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.getProbabilities();
	}

}
