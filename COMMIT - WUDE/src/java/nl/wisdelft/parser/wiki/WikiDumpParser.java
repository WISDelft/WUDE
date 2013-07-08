/**
 * 
 */
package nl.wisdelft.parser.wiki;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import nl.wisdelft.text.BasicTextAnalyzer;
import nl.wisdelft.text.OpenNLP;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.text.WikiMarkupAnalyzer;
import nl.wisdelft.text.diff_match_patch;
import nl.wisdelft.text.diff_match_patch.Diff;
import nl.wisdelft.text.diff_match_patch.Operation;
import nl.wisdelft.text.stats.WikiMarkupStats;
import nl.wisdelft.text.stats.WikiPageStats;
import opennlp.tools.util.InvalidFormatException;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import de.drni.readability.phantom.Readability;
import de.drni.readability.phantom.analysis.TextAnalyzer;
import de.drni.readability.phantom.analysis.TextStats;

/**
 * @author oosterman
 */
public class WikiDumpParser implements Iterator<WikiPageStats> {

	private String filePath = null;
	private XMLStreamReader2 reader = null;
	public int pageCount = 0;
	private int currentPage = 0;
	private Language lang;

	final String TITLE = "title";
	final String REVISION = "revision";
	final String TIMESTAMP = "timestamp";
	final String IP = "ip";
	final String USERNAME = "username";
	final String TEXT = "text";
	final String ID = "id";
	final String MINOR = "minor";
	final String PAGE = "page";
	final String REDIRECT = "redirect";
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public static void main(String[] args) throws InvalidFormatException, IOException, XMLStreamException, InterruptedException {
		String dumpFilePath = "/Users/oosterman/Google Drive/WISresearch/WUDE/Cit wiki dump+logs/Dumps/06-06-2013/wiki_full_dump.xml";
		String outputFilePath = "/Users/oosterman/Documents/Data/output/TextStatistics.txt";

		if (args.length > 0) {
			dumpFilePath = args[0];
			outputFilePath = "./TextStatistics.txt";
		}

		WikiDumpParser dumpParser = new WikiDumpParser(dumpFilePath, Language.DUTCH);
		int count = 1;
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, false));
		String delimiter = "\t";
		writer.write(WikiPageStats.toDelimitedStringHeader(delimiter));
		writer.newLine();
		while (dumpParser.hasNext()) {
			WikiPageStats stats = dumpParser.next();
			count++;
			// skip special pages
			if (stats.title.contains(":")) {
				String prefix = stats.title.substring(0, stats.title.indexOf(":") + 1);
				if (WikiMarkupAnalyzer.specialPagePrefixDutch.contains(prefix)) continue;
			}
			// skip redirect pages
			if (stats.isRedirect) continue;
			// skip empty pages
			if (stats.textStatsCurrentVersion.getNumWords() == 0) continue;

			writer.write(stats.toDelimitedString(delimiter));
			writer.newLine();
			System.out.println(count + "/" + dumpParser.pageCount);
			writer.flush();
		}
		writer.close();

	}

	/**
	 * Prepares the dump reader
	 * 
	 * @param filePath
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public WikiDumpParser(String filePath, Language lang) throws FileNotFoundException, XMLStreamException {
		this.lang = lang;
		File file = new File(filePath);
		if (!file.isFile()) throw new FileNotFoundException(filePath);

		this.filePath = filePath;
		reset();
		// read once through the dump to get the number of pages
		pageCount = 0;
		while (reader.hasNext()) {
			int eventType = reader.next();
			if (eventType == XMLEvent.START_ELEMENT && TITLE.equals(reader.getLocalName().toString())) pageCount++;
		}
		reset();

	}

	/**
	 * Resets the iterator to the beginning.
	 * 
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 */
	public void reset() throws FileNotFoundException, XMLStreamException {
		XMLInputFactory2 xmlif2 = (XMLInputFactory2) XMLInputFactory2.newInstance();
		reader = (XMLStreamReader2) xmlif2.createXMLStreamReader(filePath, new FileInputStream(filePath));
		currentPage = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return currentPage < pageCount;
	}

	public void skip(int nrItems) {
		int skipped = 0;
		String content = "";
		String curElement;
		try {
			while (reader.hasNext()) {
				int eventType = reader.next();
				switch (eventType) {
					case XMLEvent.END_ELEMENT:
						curElement = reader.getLocalName().toString();
						if (curElement.equals(PAGE)) {
							skipped++;
							currentPage++;
						}
						break;
					case XMLEvent.CHARACTERS:
						content = reader.getText();
						break;
				}
				if (skipped == nrItems) {
					System.out.println("Skipped " + nrItems + " items");
					return;
				}
			}
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public WikiPageStats next() {
		// locate the first starting title <title> tag and keep processing until the
		// closing page </page> tag
		String curElement = null;
		String content = null;
		boolean textStart = false;
		WikiPage page = null;
		Revision revision = null;

		boolean completed = false;

		int eventType;
		try {
			while (reader.hasNext() && !completed) {
				eventType = reader.next();
				switch (eventType) {
					case XMLEvent.START_ELEMENT:
						curElement = reader.getLocalName().toString();
						if (curElement.equals(TEXT)) {
							content = "";
							textStart = true;
							try {
								revision.lengthInBytes = reader.getAttributeAsInt(reader.getAttributeIndex(null, "bytes"));
							}
							catch (XMLStreamException ex) {}
						}
						else if (curElement.equals(REVISION)) {
							revision = new Revision(lang);
						}
						else if (curElement.equals(MINOR)) revision.minorRevision = true;
						else if (curElement.equals(REDIRECT)) page.isRedirect = true;
						break;
					case XMLEvent.CHARACTERS:
						// some large character blocks are split up
						if (textStart) content += reader.getText();
						else content = reader.getText();
						break;
					case XMLEvent.END_ELEMENT:
						curElement = reader.getLocalName().toString();
						// store the title
						if (curElement.equals(TITLE)) {
							page = new WikiPage(content);
						}
						else if (curElement.equals(ID)) {
							if (page.id == 0) {
								page.id = Integer.parseInt(content);
							}
							else if (revision != null && revision.id == 0) {
								revision.id = Integer.parseInt(content);
							}
						}
						else if (curElement.equals(TIMESTAMP)) {
							try {
								revision.date = dateFormat.parse(content);
							}
							catch (ParseException e) {
								e.printStackTrace();
							}
						}
						else if (curElement.equals(IP) || curElement.equals(USERNAME)) revision.user = content;
						else if (curElement.equals(TEXT)) {
							textStart = false;
							// get previous revision text, if exists
							String prevContent = null;
							if (page.revisions.size() > 0) {
								prevContent = page.revisions.get(page.revisions.size() - 1).text;
								// do not keep the old text
								page.revisions.get(page.revisions.size() - 1).text = null;
							}
							revision.text = content;
							revision.processContent(content, prevContent);
						}
						else if (curElement.equals(REVISION)) {
							page.revisions.add(revision);
						}
						else if (curElement.equals(PAGE)) {
							completed = true;
						}
						break;
					case XMLEvent.END_DOCUMENT:
				}
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
			return null;
		}
		currentPage++;
		return analyse(page);
	}

	private WikiPageStats analyse(WikiPage page) {
		WikiPageStats stats = new WikiPageStats();
		stats.title = page.title;
		stats.id = page.id;
		stats.nrRevisions = page.revisions.size();
		stats.isRedirect = page.isRedirect;
		Set<String> contributors = new HashSet<String>();
		int totalNrCharactersChanged = 0;
		int totalDiffDelete = 0;
		int totalDiffInsert = 0;
		for (Revision rev : page.revisions) {
			// unique contributors
			contributors.add(rev.user);
			// major / minor revision
			if (rev.isMajorRevision()) stats.majorRevisions++;
			else stats.minorRevisions++;
			// changes per revision
			totalNrCharactersChanged += Math.abs(rev.changeInLengthPreviousRevision);
			totalDiffDelete += rev.nrDeleteDiffsPreviousRevision;
			totalDiffInsert += rev.nrInsertDiffsPreviousRevision;
		}
		stats.averageNrCharacterChangedPerRevision = totalNrCharactersChanged / stats.nrRevisions;
		stats.averageDiffDeletePerRevision = totalDiffDelete / stats.nrRevisions;
		stats.averageDiffInsertPerRevision = totalDiffInsert / stats.nrRevisions;
		stats.nrContributors = contributors.size();
		Revision latestRev = page.getLatestRevision();
		stats.readabilityCurrentVersion = latestRev.readabilityStats;
		stats.textStatsCurrentVersion = latestRev.textStat;
		stats.markupStatsCurrentVersion = latestRev.wikiMarkupStats;
		return stats;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

class WikiPage {
	public String title;
	public int id;
	public List<Revision> revisions = new ArrayList<Revision>();
	public boolean isRedirect = false;
	public String redirectTo  = null;

	public WikiPage(String title) {
		if (title == null || title.trim().length() == 0) throw new NullPointerException("Page title cannot be null or empty");
		this.title = title;
	}

	public Revision getLatestRevision() {
		if (revisions.size() == 0) return null;
		else if (revisions.size() == 1) return revisions.get(0);
		else {
			Revision latest = null;
			for (Revision rev : revisions) {
				if (latest == null || rev.id > latest.id) latest = rev;
			}
			return latest;
		}
	}

	@Override
	public String toString() {
		String output = "Page: " + title + ", Revisions: " + revisions.size() + "\n===========\n ";
		for (Revision r : revisions) {
			output += r.toString();
			output += "\n";
		}
		return output;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof WikiPage)) return false;
		else {
			return ((WikiPage) other).title.equals(this.title);
		}
	}

	@Override
	public int hashCode() {
		return title.hashCode();
	}
}

class Revision {
	public int id;
	public Date date;
	public String user;
	public int lengthInBytes;
	public String text;
	public TextStats textStat;
	public WikiMarkupStats wikiMarkupStats;
	public Readability readabilityStats;

	private Language lang;
	private OpenNLP openNLP;

	private static TextAnalyzer analyzer = new TextAnalyzer();

	/**
	 * Whether the revision was tagged as minor revision
	 */
	public boolean minorRevision = false;
	/**
	 * Diffs using diff_match_patch from google
	 */
	public int nrInsertDiffsPreviousRevision;
	public int nrDeleteDiffsPreviousRevision;
	/**
	 * Differences in length of the content
	 */
	public int changeInLengthPreviousRevision;

	public static diff_match_patch diff = new diff_match_patch();

	public Revision(Language lang) {
		this.lang = lang;
		openNLP = new OpenNLP(lang);
	}

	public void processContent(String content, String prevContent) {
		if (prevContent != null) {
			List<Diff> diffs = diff.diff_main(prevContent, content);
			for (Diff diff : diffs) {
				if (diff.operation == Operation.DELETE) nrDeleteDiffsPreviousRevision++;
				else if (diff.operation == Operation.INSERT) nrInsertDiffsPreviousRevision++;
			}
			changeInLengthPreviousRevision = content.length() - prevContent.length();
		}

		textStat = BasicTextAnalyzer.analyze(content, lang);
		wikiMarkupStats = WikiMarkupAnalyzer.analyse(content);
		readabilityStats = new Readability(textStat);
	}

	public boolean isMajorRevision() {
		// if tagged as minor
		if (minorRevision) return false;
		// if the page is new (no diffs with previous version) it is major
		if (nrDeleteDiffsPreviousRevision + nrInsertDiffsPreviousRevision == 0) {
			return true;
		}
		// people might forget to tag "minor" even if the revision was minor.
		// heuristics: if length increase > 200 || < -200 there is a new paragraph
		// or a deleted paragraph (and thus major)
		if (changeInLengthPreviousRevision > 200 || changeInLengthPreviousRevision < -200) return true;
		// if many edits have been performed (> 10 diffs) it is major
		if (nrDeleteDiffsPreviousRevision + nrInsertDiffsPreviousRevision > 10) return true;

		// else consider it as a minor revision
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof Revision)) return false;
		else {
			return ((Revision) other).id == (this.id);
		}
	}

	@Override
	public int hashCode() {
		return id;
	}
}
