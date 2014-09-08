/**
 * 
 */
package nl.wisdelft.wiki.analysis;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import nl.wisdelft.text.BasicTextAnalyzer;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.text.Readability;
import nl.wisdelft.text.Stats;
import nl.wisdelft.wiki.entity.Revision;
import nl.wisdelft.wiki.entity.RevisionComparison;
import nl.wisdelft.wiki.entity.WikiMarkupStats;
import nl.wisdelft.wiki.entity.WikiPage;
import nl.wisdelft.wiki.entity.WikiPageStats;

/**
 * @author oosterman
 */
public class WikiPageAnalyzer {
	public static final Set<String> specialPagePrefixDutch = new HashSet<String>(Arrays.asList(new String[] { "Media:", "Speciaal:",
			"Overleg:", "Gebruiker:", "Overleg gebruiker:", "WikiDelft:", "Overleg WikiDelft:", "Bestand:", "Overleg bestand:", "MediaWiki:",
			"Overleg MediaWiki:", "Sjabloon:", "Overleg sjabloon:", "Help:", "Overleg help:", "Categorie:", "Overleg categorie:" }));

	private static WikiModel wikiModel = new WikiModel("http://wikidelft.nl/index.php?title=${image}", "http://wikidelft.nl/index.php?title=${title}");

	private WikiPageAnalyzer() {}

	public static boolean isSpecialPage(String title) {
		// empty title
		if (title == null || title.isEmpty()) return false;
		// does not contain a colon :
		int indexColon = title.indexOf(":");
		if (indexColon < 0) return false;
		// does not match special pages prefixes
		String prefix = title.substring(0, indexColon + 1);
		return specialPagePrefixDutch.contains(prefix);
	}

	public static String getPlainTextContent(String wikiText){
    String plainStr = wikiModel.render(new PlainTextConverter(), wikiText);
    return plainStr;
	}

	public static WikiPageStats analyse(WikiPage page, Language lang) {
		WikiPageStats stats = new WikiPageStats();
		stats.setTitle(page.getTitle());
		stats.setId(page.getId());
		stats.setNrRevisions(page.getRevisions().size());
		stats.setRedirect(page.isRedirect());

		// analyze first rev
		Revision firstRev = page.getFirstRevision();
		if (firstRev != null) stats.setCreationDate(firstRev.getDate());

		// analyze last rev
		Revision lastRev = page.getLatestRevision();
		Stats textStats = BasicTextAnalyzer.analyze(lastRev.getText(), lang);
		Readability readability = new nl.wisdelft.text.Readability(textStats);
		WikiMarkupStats markupStats = WikiMarkupAnalyzer.analyse(lastRev.getText());
		stats.setReadabilityLastestRevision(readability);
		stats.setMarkupStatsLastestRevision(markupStats);

		// analyze all revs
		Set<String> contributors = new HashSet<String>();
		int totalNrCharactersChanged = 0;
		int totalDiffDelete = 0;
		int totalDiffInsert = 0;
		int minorRevisions = 0;
		int majorRevisions = 0;
		Revision prevRev = null;
		RevisionComparison comp;
		for (Revision rev : page.getRevisions()) {
			// unique contributors
			contributors.add(rev.getUser());
			comp = rev.compareToRevision(prevRev);
			if (comp.isMajorChanges()) majorRevisions++;
			else minorRevisions++;
			totalNrCharactersChanged += Math.abs(comp.getNrCharactersChanged());
			totalDiffDelete += comp.getNrDeleteDiffsPreviousRevision();
			totalDiffInsert += comp.getNrDeleteDiffsPreviousRevision();
			prevRev = rev;
		}

		stats.setMinorRevisions(minorRevisions);
		stats.setMajorRevisions(majorRevisions);
		stats.setAverageDiffDeletePerRevision(totalDiffDelete / page.getRevisions().size());
		stats.setAverageDiffInsertPerRevision(totalDiffInsert / page.getRevisions().size());
		stats.setAverageNrCharactersChangedPerRevision(totalNrCharactersChanged / page.getRevisions().size());
		stats.setNrContributors(contributors.size());
		return stats;
	}

}
