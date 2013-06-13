/**
 * 
 */
package nl.wisdelft.text;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import nl.wisdelft.parser.wiki.WikiDumpParser;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.text.stats.SentimentStats;
import nl.wisdelft.text.stats.WikiMarkupStats;
import nl.wisdelft.text.stats.WikiPageStats;
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
		/*
		 * String wikiMarkup =
		 * "[[Image:Oude Kerk Delft 2.jpg|thumb|right|thumb|The leaning tower]]\n[[Image:Delft - Oude Kerk - interieur.jpg|thumb|Interior of the church]]\n\nThe '''Oude Kerk''' (Old Church), nicknamed ''Oude Jan'' (\"Old John\"), is a [[Gothic architecture|Gothic]] [[Protestant Church in the Netherlands|Protestant]] church in the old city center of [[Delft]], the [[Netherlands]]. Its most recognizable feature is a 75-meter-high brick tower that leans about two meters from the vertical.\n\n==History==\nThe Oude Kerk was founded as [[St. Bartholomew]]'s Church in the year 1246, on the site of previous churches dating back up to two centuries earlier. The layout followed that of a traditional [[basilica]], with a [[nave]] flanked by two smaller [[aisle]]s.\n\nDuring its build the foundations weren't strong enough to support the building, and the church began to lean. As they continued to build the church they tried to compensate its lean on each layer of the tower, but it remains to this day that only the 4 turrets at the top are truly vertical.\n\nThe tower with its central spire and four corner turrets was added between 1325Ð50, and dominated the townscape for a century and a half until it was surpassed in height by the [[Nieuwe Kerk (Delft)|Nieuwe Kerk]] (New Church). It is possible that the course of the adjacent canal had to be shifted slightly to make room for the tower, leaving an unstable foundation that caused the tower to tilt.\n\nBy the end of the 14th century, expansion of the side aisles to the height of the nave transformed the building into a [[hall church]], which was rededicated to [[Hippolytus (writer)|St. Hippolytus]]. The church again took on a typical basilican cross-section with the construction of a higher nave between about 1425 and 1440.\n\nThe Delft town fire of 1536 and the turmoil of the [[Protestant Reformation]] brought a premature end to an ambitious expansion project led by two members of the [[Keldermans family]] of master builders. This construction phase resulted in the flat-roofed, stone-walled northern [[transept]] arm that differs markedly in style from the older parts.\n\nThe great fire, [[iconoclasm]], weather, and the explosion of the town's gunpowder store in 1654 (''see [[Delft Explosion]]'') took their toll on the church and its furnishings, necessitating much repair work over the years. During one renovation, the tower turrets were rebuilt in a more vertical alignment than the leaning body below, giving the tower as a whole a slightly kinked appearance. The current [[stained glass|stained-glass windows]] were crafted by the master glazier Joep Nicolas in the mid-20th century.\n\n==Furnishings==\nThe church possesses three [[pipe organ]]s, from the years 1857 (main organ), 1873 (north aisle) and 1770 (choir).\n\nThe most massive [[church bell|bell]] in the tower, cast in 1570 and called ''Trinitasklok'' or ''Bourdon'', weighs nearly nine [[tonne]]s, and because of its strong and potentially damaging vibrations, is rung only on such special occasions as the burial of a [[Dutch monarchy|Dutch royal family]] member in the nearby New Church. The massive bell is also sounded during disasters, when local [[Civil defense siren|air-raid sirens]] are sounded. This, however, does not happen during the siren's monthly, country-wide test, which happens every first Monday of the month.\n\n==Graves==\nApproximately 400 people are entombed in the Oude Kerk, including the following notables:\n\n* Elizabeth Morgan, daughter of nobleman [[Marnix van St. Aldegonde]] (1608)\n* noblewoman and benefactrix [[Clara van Spaerwoude]] (1615)\n* naval hero [[Piet Pieterszoon Hein|Piet Hein]] (1629)\n* writer [[Jan Stalpaert van der Wiele]] (1630)\n* naval hero [[Maarten Tromp]] (1653)\n* physician/anatomist [[Regnier de Graaf]] (1673)\n* painter [[Johannes Vermeer]] (1675)\n* painter [[Hendrick Cornelisz van Vliet]], who had painted the church interior (1675)\n* statesman [[Anthonie Heinsius]] (1720)\n* scientist [[Anton van Leeuwenhoek]] (1723)\n* poet [[Hubert Poot]] (1733)\n\n== External links ==\n{{Commons category|Oude Kerk (Delft)}}\n* [http://www.oudekerk-delft.nl/eng/kerkgebouw/index.html Oude Kerk website]\n* [http://www.wga.hu/art/h/heyden/view_d.jpg View of the Oude Kerk] painted by Jan van der Heyden, ca. 1660 (Web Gallery of Art)\n\n{{coord|52|0|45|N|4|21|19|E|type:landmark_region:NL-ZH|display=title}}\n\n[[Category:14th-century church buildings]]\n[[Category:Towers completed in the 14th century]]\n[[Category:Churches in the Netherlands]]\n[[Category:Rijksmonuments in Delft]]\n[[Category:Brick Gothic]]\n[[Category:Inclined towers]]\n[[Category:Churches in South Holland]]\n"
		 * ; TextStatistics stats = new TextStatistics(wikiMarkup, Language.DUTCH);
		 * stats.analyze();
		 */
		String dumpFilePath = "/Users/oosterman/Google Drive/WISresearch/WUDE/wiki_dump_20130606/wiki_full_dump.xml";
		String outputFilePath = "/Users/oosterman/Documents/Data/output/TextStatistics.txt";
		
		if(args.length>0){
			dumpFilePath = args[0];
			outputFilePath = "./TextStatistics.txt";
		}
		
		Set<String> specialPrefixes = new HashSet<String>(Arrays.asList(new String[] { "Media:", "Speciaal:", "Overleg:", "Gebruiker:",
				"Overleg gebruiker:", "WikiDelft:", "Overleg WikiDelft:", "Bestand:", "Overleg bestand:", "MediaWiki:", "Overleg MediaWiki:",
				"Sjabloon:", "Overleg sjabloon:", "Help:", "Overleg help:", "Categorie:", "Overleg categorie:" }));

		WikiDumpParser dumpParser = new WikiDumpParser(dumpFilePath, Language.DUTCH);
		int count = 1;
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath,true));
		String del = "\t";
		dumpParser.skip(2022);
		while (dumpParser.hasNext()) {
			WikiPageStats stats = dumpParser.next();
			count++;
			if(stats == null)
				continue;
			if(stats.title.contains(":")){
				String prefix = stats.title.substring(0,stats.title.indexOf(":")+1);
				if(specialPrefixes.contains(prefix)) continue;
			}
			writer.write(stats.toDelimitedString(del));
			writer.newLine();
			System.out.println(count + "/" + dumpParser.pageCount);
			writer.flush();
		}
		writer.close();
		
	}
}
