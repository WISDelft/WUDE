/**
 * 
 */
package nl.wisdelft.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.wisdelft.UserProfile;
import nl.wisdelft.UserProfile.ValueType;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 * @author oosterman
 */
public class Gazatteer {
	private String wordlistPath = null;
	private String wordlistSuffix = null;
	private boolean recursive = true;
	private String contentFieldname = null;
	private String indexPath = null;
	private Set<String> additionalExcludedTerms = null;
	private String outputPath = null;
	private String outputDoubleFormat = "%.4f";
	private static int nrTopDocuments = 2000;

	public enum StorageMethod {
		DISK, GAE_USERPROFILE
	};

	public Gazatteer() {

	}

	/**
	 * @param wordlistPath Directory containing wordlists
	 * @param wordlistSuffix The suffix (eg. ".txt") of wordlists
	 * @param recursive Whether to check for wordlists in subfolders
	 * @param indexPath Directory containing the index
	 * @param contentFieldname Name of the field to search on
	 * @param additionalExcludedTerms Additional excluded terms for wordlists
	 * @throws FileNotFoundException
	 */
	public Gazatteer(String wordlistPath, String wordlistSuffix, boolean recursive, String indexPath, String contentFieldname, Set<String> additionalExcludedTerms)
			throws FileNotFoundException {
		setWordlistPath(wordlistPath);
		setWordlistSuffix(wordlistSuffix);
		setRecursive(recursive);
		setIndexPath(indexPath);
		setContentFieldname(contentFieldname);
		setAdditionalExcludedTerms(additionalExcludedTerms);
	}

	/**
	 * @throws IOException When there is an error processing wordlists
	 */
	public void startGazetteering(StorageMethod method) throws IOException {
		long now = System.currentTimeMillis();
		File dir = new File(wordlistPath);
		// get all wordlists
		Map<String, List<String>> wordlists = new HashMap<String, List<String>>();
		readWordlistDirectory(dir, recursive, wordlists);
		System.out.println("All wordlists are read.");
		// create the storage for the output
		// the dimensions
		List<String> columns = new ArrayList<String>(wordlists.keySet());
		// the values Map<user, Map<dimension,value>>
		Map<String, Map<String, String>> rows = new HashMap<String, Map<String, String>>();
		// foreach wordlist check the index
		for (String wordlist : wordlists.keySet()) {
			// a map of user - value pairs
			Map<String, String> scores = search(wordlist, wordlists.get(wordlist));
			// add scores to output storage
			for (String user : scores.keySet()) {
				if (!rows.containsKey(user)) {
					rows.put(user, new HashMap<String, String>());
				}
				// add the value
				rows.get(user).put(wordlist, scores.get(user));
			}

			if (method == StorageMethod.GAE_USERPROFILE) {
				System.out.print(String.format("\tStoring %s values...", scores.size()));
				boolean success = UserProfile.setBulkUserProfileEntries(wordlist, null, ValueType.Double, "gazateer", scores);
				if (!success) {
					throw new IOException("There is a problem with the datastore. Could not store all data.");
				}
				System.out.println("\tStored.");
			}
		}
		if (method == StorageMethod.DISK) {
			System.out.print("All scored are calculated. Storing ...");
			storeResultsInFile(columns, rows);
			System.out.println("\tStored");
		}
		System.out.println(String.format("Gazatteering completed in %s seconds", (System.currentTimeMillis() - now) / 1000));
	}

	public void countOccurences() throws Exception {
		long now = System.currentTimeMillis();
		File dir = new File(wordlistPath);
		// get all wordlists
		Map<String, List<String>> wordlists = new HashMap<String, List<String>>();
		readWordlistDirectory(dir, recursive, wordlists);
		System.out.println("All wordlists are read.");
		// storage for count per term in a wordlist
		Map<String, Integer> counts = new HashMap<String, Integer>();
		// create the searcher
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		// foreach wordlist check the index
		Query q;
		TopDocs t;
		// for each wordlist
		for (String wordlist : wordlists.keySet()) {
			List<String> values = wordlists.get(wordlist);
			// for each term in the wordlist
			for (String value : values) {
				q = parseSearchString(value, getAdditionalExcludedTerms());
				if (q != null) {
					t = searcher.search(q, nrTopDocuments);
					counts.put(q.toString(contentFieldname), t.totalHits);
				}
			}
			System.out.println("\tCounted all " + values.size() + " terms from " + wordlist);
		}
		// store the result
		String delimiter = "\t";
		String newline = "\n";
		File f = new File(outputPath);
		BufferedWriter writer = new BufferedWriter(new FileWriter(f, false));
		for (String word : counts.keySet()) {
			writer.write(word);
			writer.write(delimiter);
			writer.write(Integer.toString(counts.get(word)));
			writer.write(newline);
		}
		writer.flush();
		writer.close();

	}

	private void storeResultsInFile(List<String> columns, Map<String, Map<String, String>> rows) throws IOException {
		String delimiter = ", ";
		String newline = "\n";
		File f = new File(outputPath);
		BufferedWriter writer = new BufferedWriter(new FileWriter(f, false));
		// write header row starting with #
		StringBuilder builder = new StringBuilder();
		builder.append("# user");
		for (String column : columns) {
			builder.append(delimiter);
			builder.append(column);
		}
		builder.append(newline);
		// write the rows
		for (String user : rows.keySet()) {
			Map<String, String> userData = rows.get(user);
			builder.append(user);
			for (String column : columns) {
				builder.append(delimiter);
				if (userData.containsKey(column)) builder.append(userData.get(column));
				else builder.append("0");
			}
			builder.append(newline);
		}
		writer.write(builder.toString());
		writer.flush();
		writer.close();
	}

	/**
	 * Reads the specified directory, optionally recursive, and extracts all terms
	 * from these files and stored them in
	 * 
	 * @param wordlists
	 * @param dir The directory containing the files
	 * @param recursive Whether subdirectories should be checked
	 * @param wordlists A Map with the filename as key
	 * @throws IOException When the wordlists cannot be read
	 */
	private void readWordlistDirectory(File dir, boolean recursive, Map<String, List<String>> wordlists) throws IOException {
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				// check all subfolders if recursive
				if (f.isDirectory() && recursive) readWordlistDirectory(f, recursive, wordlists);
				else if (f.isFile() && f.getName().endsWith(wordlistSuffix)) {
					BufferedReader reader = new BufferedReader(new FileReader(f));
					String key = f.getName().replace(wordlistSuffix, "");
					if (!wordlists.containsKey(key)) {
						wordlists.put(key, new ArrayList<String>());
					}
					List<String> list = wordlists.get(key);
					while (reader.ready()) {
						list.add(reader.readLine());
					}
					reader.close();
				}
				else continue;
			}
		}
	}

	/**
	 * Creates a Query from {@code term}. Multiple words in are converted in a
	 * {@see PhraseQuery} and stopwords and terms from {@code excludedTerms} are
	 * removed. Words shorter then two characters are removed.
	 * 
	 * @param term
	 * @param excludedTerms
	 * @return
	 */
	private Query parseSearchString(String term, Set<String> excludedTerms) {
		// to lowercase
		term = term.toLowerCase();
		// remove stopwords, preserve the order
		Collection<String> words = StopWords.removeStopwords(term, true, excludedTerms);

		// if there are more than one words make a phrasequery
		if (words.size() > 1) {
			PhraseQuery pq = new PhraseQuery();
			pq.setSlop(4);
			for (String word : words) {
				if (word.length() > 1) pq.add(new Term(contentFieldname, word));
			}
			return pq;
		}
		// else make a termquery
		else if (words.size() == 1) {
			String word = words.iterator().next();
			if (word.length() > 1) {
				TermQuery tq = new TermQuery(new Term(contentFieldname, word));
				return tq;
			}
		}

		return null;

	}

	private Map<String, String> search(String termsetName, List<String> terms) throws IOException {
		System.out.print(String.format("\tSearching for %s terms from %s", terms.size(), termsetName));

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);

		Query q;
		BooleanQuery bq = new BooleanQuery();
		BooleanQuery.setMaxClauseCount(terms.size());
		for (String term : terms) {
			q = parseSearchString(term, additionalExcludedTerms);
			if (q != null) bq.add(new BooleanClause(q, Occur.SHOULD));
		}

		TopDocs docs = searcher.search(bq, nrTopDocuments);

		System.out.println(String.format("\tFound %s matching documents", docs.totalHits));

		Document d;
		File f;
		Map<String, String> scores = new HashMap<String, String>();
		for (ScoreDoc doc : docs.scoreDocs) {
			d = searcher.doc(doc.doc);
			f = new File(d.get("path"));
			scores.put(f.getName(), String.format(outputDoubleFormat, doc.score));
		}
		return scores;
	}

	public Set<String> getAdditionalExcludedTerms() {
		return additionalExcludedTerms;
	}

	/**
	 * Adds terms to exclude from the gazatteer lists
	 * 
	 * @param additionalExcludedTerms
	 */
	public void setAdditionalExcludedTerms(Set<String> additionalExcludedTerms) {
		this.additionalExcludedTerms = additionalExcludedTerms;
	}

	public String getWordlistPath() {
		return wordlistPath;
	}

	public void setWordlistPath(String wordlistPath) throws FileNotFoundException {
		File dir = new File(wordlistPath);
		if (!dir.exists() || !dir.isDirectory()) throw new FileNotFoundException(String.format("Directory '%s' does not exist.", dir));
		this.wordlistPath = wordlistPath;
	}

	public String getWordlistSuffix() {
		return wordlistSuffix;
	}

	public void setWordlistSuffix(String wordlistSuffix) {
		this.wordlistSuffix = wordlistSuffix;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public String getContentFieldname() {
		return contentFieldname;
	}

	public void setContentFieldname(String contentFieldname) {
		this.contentFieldname = contentFieldname;
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) throws FileNotFoundException {
		File dir = new File(indexPath);
		if (!dir.exists() || !dir.isDirectory()) throw new FileNotFoundException(String.format("Directory '%s' does not exist.", dir));
		this.indexPath = indexPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
}
