/**
 * 
 */
package nl.wisdelft.wiki.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.wisdelft.wiki.entity.WikiMarkupStats;

/**
 * @author oosterman
 */
class WikiMarkupAnalyzer {

	private static Pattern h2Pattern = java.util.regex.Pattern.compile("==[^=]+==[^=]");
	private static Pattern h3Pattern = java.util.regex.Pattern.compile("===[^=]+===[^=]");
	private static Pattern h4Pattern = java.util.regex.Pattern.compile("====[^=]+====[^=]");
	private static Pattern h5Pattern = java.util.regex.Pattern.compile("=====[^=]+=====[^=]");
	private static Pattern h6Pattern = java.util.regex.Pattern.compile("======[^=]+======[^=]");
	private static Pattern intLinkPattern = java.util.regex.Pattern.compile("\\[\\[[^\\]]*\\]\\]", Pattern.CASE_INSENSITIVE);
	private static Pattern extLinkPattern = java.util.regex.Pattern.compile("(\\[){0,1}(http|https)://");
	private static Pattern collectionLinkPattern = java.util.regex.Pattern.compile("(http|https)://test.wikidelft.nl/ccscripts/imageproxyImageInPage.php");
	private static Pattern filePattern = java.util.regex.Pattern.compile("\\[\\[(bestand|file|image):", Pattern.CASE_INSENSITIVE);
	private static Pattern categoryPattern = java.util.regex.Pattern.compile("\\[\\[(categorie|category):([^\\]]*)\\]\\]",
			Pattern.CASE_INSENSITIVE);
	private static Pattern referencesPattern = java.util.regex.Pattern.compile("<ref>");
	private static Pattern boldItalicsPatter = java.util.regex.Pattern.compile("'''''[^']+'''''[^']");
	private static Pattern boldPatterns = java.util.regex.Pattern.compile("'''[^']+'''[^']");
	private static Pattern italicsPattern = java.util.regex.Pattern.compile("''[^']+''[^']");

	

	private WikiMarkupAnalyzer() {}

	public static WikiMarkupStats analyse(String text) {
		// initialize the stats storage
		WikiMarkupStats stats = new WikiMarkupStats();

		// perform manual magic to get the h2-h6 count and special WikiDelft
		// structures.
		Matcher h2 = h2Pattern.matcher(text);
		while (h2.find())
			stats.h2++;

		Matcher h3 = h3Pattern.matcher(text);
		while (h3.find())
			stats.h3++;

		Matcher h4 = h4Pattern.matcher(text);
		while (h4.find())
			stats.h4++;

		Matcher h5 = h5Pattern.matcher(text);
		while (h5.find())
			stats.h5++;

		Matcher h6 = h6Pattern.matcher(text);
		while (h6.find())
			stats.h6++;

		Matcher intLink = intLinkPattern.matcher(text);
		while (intLink.find())
			stats.internalLinks++;

		Matcher collectionLink = collectionLinkPattern.matcher(text);
		while (collectionLink.find())
			stats.collectionItemLink++;

		Matcher extLink = extLinkPattern.matcher(text);
		while (extLink.find())
			stats.externalLinks++;
		// subtract the collection links from the external links
		stats.externalLinks -= stats.collectionItemLink;

		Matcher file = filePattern.matcher(text);
		while (file.find())
			stats.images++;

		Matcher category = categoryPattern.matcher(text);
		while (category.find())
			stats.categories.add(category.group(2));

		Matcher reference = referencesPattern.matcher(text);
		while (reference.find())
			stats.references++;

		Matcher bold = boldPatterns.matcher(text);
		while (bold.find())
			stats.bold++;

		Matcher boldItalics = boldItalicsPatter.matcher(text);
		while (boldItalics.find())
			stats.boldItalics++;

		Matcher italics = italicsPattern.matcher(text);
		while (italics.find())
			stats.italics++;

		// correct internal links for categories and files
		stats.internalLinks -= stats.images + stats.categories.size();

		return stats;
	}

}
