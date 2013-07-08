/**
 * 
 */
package nl.wisdelft.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import nl.wisdelft.parser.wiki.Wiki;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.text.stats.SentimentStats;
import nl.wisdelft.text.stats.WikiMarkupStats;
import opennlp.tools.util.InvalidFormatException;
import de.drni.readability.phantom.analysis.TextStats;

/**
 * @author oosterman
 */
public class TextStatistics {
	private String text;
	private Language lang;
	private OpenNLP openNLP;

	public TextStatistics(String text, Language lang) throws InvalidFormatException, IOException {
		this.text = text;
		this.lang = lang;
		openNLP = new OpenNLP(lang);
	}

	public void analyze() throws IOException {
		// Basic operations
		Iterable<String> tokensWhitespace = Arrays.asList(openNLP.tokenizeWhitespace(text));;
		Iterable<String> tokensLanguage = Arrays.asList(openNLP.tokenize(text));

		// Basic textual features
		TextStats textStats = BasicTextAnalyzer.analyze(text, lang);

		// Readability features
		de.drni.readability.phantom.Readability readabilityStats = new de.drni.readability.phantom.Readability(textStats);

		// Sentiment features
		SentimentStats sentimentStats = SentimentAnalyzer.analyze(tokensWhitespace);

		// Wiki features
		WikiMarkupStats wikiStats = WikiMarkupAnalyzer.analyse(text);

		return;
	}

	public static void main(String[] args) throws InvalidFormatException, IOException, XMLStreamException, InterruptedException {
		// read in all the titles from the parsed dump
		List<String> pageTitles = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(new File("/Users/oosterman/Desktop/wiki_r_analysis/TextStatistics.txt")));
		String line = null;
		String[] features;
		String title;
		// skip first line
		reader.readLine();
		while ((line = reader.readLine()) != null) {
			features = line.split("\t");
			title = features[0];
			title = title.substring(1, title.length() - 1);
			pageTitles.add(title);
		}

		// we have all the titles. Perform a search using three algorithms
		// 1: exact match
		// 2: First result search on page title
		// 3: First result on search page title +"Delft"
		Wiki wiki = new Wiki("nl.wikipedia.org");
		wiki.setUserAgent("Wiki.java" + wiki.version() + " (j.e.g.oosterman@tudelft.nl)");

		// use this writer to write out the maching pages
		BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/oosterman/Documents/Data/output/wikiMatches.txt"));

		String wpTitle = null;
		boolean found;
		for (String pageTitle : pageTitles) {
			// perform an exact search for the page
			Map exactMatch = wiki.getPageInfo(pageTitle);
			// search for the pageTitle (first hit)
			String[][] searchResult = wiki.search(pageTitle);
			// search for the pagetitle together with Delft
			String[][] searchResultsDelft = wiki.search(pageTitle + " Delft");

			// wikipedia page title
			found = false;
			if ((Boolean) exactMatch.get("exists")) {
				found = true;
				wpTitle = (String) exactMatch.get("displayTitle");
			}
			else {
				System.out.println("Searching for page: "+pageTitle);
				if (searchResult.length > 0) {
					System.out.println("Is this the page (y/n):");
					System.out.println(searchResult[0][0]);
					System.out.println(searchResult[0][2]);
					String input = System.console().readLine();
					if (input.equals("y")) {
						found = true;
						wpTitle = searchResult[0][0];
					}
				}
				if (!found && searchResultsDelft.length > 0) {
					System.out.println("Is this the page (y/n):");
					System.out.println(searchResultsDelft[0][0]);
					System.out.println(searchResultsDelft[0][2]);
					String input = System.console().readLine();
					if (input.equals("y")) {
						found = true;
						wpTitle = searchResultsDelft[0][0];
					}
				}

				if (found) {
					writer.write("\""+pageTitle+"\"\t\""+wpTitle+"\"");
					writer.newLine();
				}
			}

		}

	}
}
