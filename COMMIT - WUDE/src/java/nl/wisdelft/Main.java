package nl.wisdelft;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import nl.wisdelft.text.Gazatteer;
import nl.wisdelft.text.Gazatteer.StorageMethod;
import nl.wisdelft.text.StopWords;
import nl.wisdelft.text.index.DirectoryIndexer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.util.Version;

/**
 * 
 */

/**
 * @author oosterman
 */
public class Main {
	static String docsPath = "/Users/oosterman/Documents/Data/userdocumentsNL";
	static String indexPath = "/Users/oosterman/Documents/Data/index";
	static String wordlistPath = "/Users/oosterman/Documents/Data/wordlists";
	static String outputPath = "/Users/oosterman/Documents/Data/output/gazatteerResult.csv";
	static String countPath = "/Users/oosterman/Documents/Data/output/gazatteerCountResult.csv";
	static String wordlistSuffix = ".txt";
	static Version version = Version.LUCENE_40;
	static Analyzer analyzer = null;
	static String contentFieldname = "contents";
	static boolean recursive = true;
	static Set<String> additionalExcludedTerms = new HashSet<String>(Arrays.asList(new String[] { ".", ",", "?", "!", "@", "#", "$", "%",
			"^", "&", "*", "(", ")", "-", "_", "=", "+", "*", "/", "+", "-", "`", "~", "<", ">", "/", "\\", ":", ";", "{", "[", "]", "}", "|",
			"bv", "b.v.", "nv", "n.v.", "v.o.f.", "RT" }));

	public static Analyzer getAnalyzer() {
		if (analyzer == null) {
			CharArraySet stopwords = CharArraySet.copy(version, StopWords.getStopwords());
			stopwords.addAll(DutchAnalyzer.getDefaultStopSet());
			analyzer = new DutchAnalyzer(version, stopwords);
		}
		return analyzer;
	}

	public static void main(String args[]) throws Exception {
		boolean doIndex = false;
		boolean doGazateer = false;
		boolean doCount = true;
		for (String arg : args) {
			if (arg.equals("index")) doIndex = true;
			else if (arg.equals("gazateer")) doGazateer = true;
			else if(arg.equals("count")) doCount = true;
		}

		if (doIndex) {
			// index all the files
			DirectoryIndexer.index(docsPath, indexPath, OpenMode.CREATE_OR_APPEND, getAnalyzer());
		}
		if (doGazateer) {
			// create the gazeteer
			Gazatteer gz = new Gazatteer(wordlistPath, wordlistSuffix, recursive, indexPath, contentFieldname, additionalExcludedTerms);
			gz.setOutputPath(outputPath);
			// run the gazateer
			gz.startGazetteering(StorageMethod.DISK);
		}
		if(doCount){
			Gazatteer gz = new Gazatteer(wordlistPath, wordlistSuffix, recursive, indexPath, contentFieldname, additionalExcludedTerms);
			gz.setOutputPath(countPath);
			gz.countOccurences();
		}
		System.out.println("Done!");
	}
}
