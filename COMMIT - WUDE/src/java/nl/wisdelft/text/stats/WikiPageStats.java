/**
 * 
 */
package nl.wisdelft.text.stats;

import de.drni.readability.phantom.Readability;
import de.drni.readability.phantom.analysis.TextStats;

/**
 * @author oosterman
 *
 */
public class WikiPageStats {
	public String title;
	public int id;
	public int nrRevisions;
	public int minorRevisions;
	public int majorRevisions;
	public int averageChangePerRevision;
	public int averageDiffDeletePerRevision;
	public int averageDiffInsertPerRevision;
	public int nrContributors;
	
	public Readability readabilityCurrentVersion;
	public WikiMarkupStats markupStatsCurrentVersion;
	public TextStats textStatsCurrentVersion;
	
	public String toDelimitedStringHeader(String delimiter){
		StringBuilder builder = new StringBuilder();
		builder.append("title");
		builder.append(delimiter);
		builder.append("nrRevisions");
		builder.append(delimiter);
		builder.append("minorRevisions");
		builder.append(delimiter);
		builder.append("majorRevisions");
		builder.append(delimiter);
		builder.append("nrContributors");
		builder.append(delimiter);
		builder.append("avgChangePerRevision");
		builder.append(delimiter);
		builder.append("avgDiffDeletePerRevision");
		builder.append(delimiter);
		builder.append("avgDiffInsertPerRevision");
		builder.append(delimiter);
		builder.append("nrChars");
		builder.append(delimiter);
		builder.append("nrWords");
		builder.append(delimiter);
		builder.append("nrSentences");
		builder.append(delimiter);
		builder.append("nrFormatting");
		builder.append(delimiter);
		builder.append("nrSections");
		builder.append(delimiter);
		builder.append("nrIntLinks");
		builder.append(delimiter);
		builder.append("nrExtLinks");
		builder.append(delimiter);
		builder.append("nrColLinks");
		builder.append(delimiter);
		builder.append("nrImages");
		builder.append(delimiter);
		builder.append("nrReferences");
		builder.append(delimiter);
		builder.append("readScoreFlesch");
		builder.append(delimiter);
		builder.append("readScoreKincaid");
		
		return builder.toString();
	}
	
	public String toDelimitedString(String delimiter){
		StringBuilder builder = new StringBuilder();
		builder.append("\"");
		builder.append(title);
		builder.append("\"");
		builder.append(delimiter);
		builder.append(nrRevisions);
		builder.append(delimiter);
		builder.append(minorRevisions);
		builder.append(delimiter);
		builder.append(majorRevisions);
		builder.append(delimiter);
		builder.append(nrContributors);
		builder.append(delimiter);
		builder.append(averageChangePerRevision);
		builder.append(delimiter);
		builder.append(averageDiffDeletePerRevision);
		builder.append(delimiter);
		builder.append(averageDiffInsertPerRevision);
		builder.append(delimiter);
		builder.append(textStatsCurrentVersion.getNumCharacters());
		builder.append(delimiter);
		builder.append(textStatsCurrentVersion.getNumWords());
		builder.append(delimiter);
		builder.append(textStatsCurrentVersion.getNumSentences());
		builder.append(delimiter);
		builder.append(markupStatsCurrentVersion.getNrFormatting());
		builder.append(delimiter);
		builder.append(markupStatsCurrentVersion.getNrSections());
		builder.append(delimiter);
		builder.append(markupStatsCurrentVersion.internalLinks);
		builder.append(delimiter);
		builder.append(markupStatsCurrentVersion.externalLinks);
		builder.append(delimiter);
		builder.append(markupStatsCurrentVersion.collectionItemLink);
		builder.append(delimiter);
		builder.append(markupStatsCurrentVersion.images);
		builder.append(delimiter);
		builder.append(markupStatsCurrentVersion.references);
		builder.append(delimiter);
		builder.append(readabilityCurrentVersion.calcFlesch());
		builder.append(delimiter);
		builder.append(readabilityCurrentVersion.calcKincaid());
		
		return builder.toString();
	
	}
}
