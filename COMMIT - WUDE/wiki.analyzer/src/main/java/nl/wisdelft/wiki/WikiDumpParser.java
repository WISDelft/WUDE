/**
 * 
 */
package nl.wisdelft.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.wiki.entity.Revision;
import nl.wisdelft.wiki.entity.WikiPage;

/**
 * @author oosterman
 */
public class WikiDumpParser implements Iterator<WikiPage> {

	private String filePath = null;
	private XMLStreamReader reader = null;
	private int pageCount = 0;
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
		XMLInputFactory xmlif = (XMLInputFactory) XMLInputFactory.newInstance();
		reader = (XMLStreamReader) xmlif.createXMLStreamReader(filePath, new FileInputStream(filePath));
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
				}
				if (skipped == nrItems) {
					break;
				}
			}
			System.out.println("Skipped " + skipped + " items");
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public WikiPage next() {
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
							String bytes = reader.getAttributeValue(null, "bytes");
							try {
								revision.setLengthInBytes(Integer.parseInt(bytes));
							}
							catch (NumberFormatException ex) {
								revision.setLengthInBytes(-1);
							}
						}
						else if (curElement.equals(REVISION)) {
							revision = new Revision(lang);
						}
						else if (curElement.equals(MINOR)) revision.setMinorRevision(true);
						else if (curElement.equals(REDIRECT)) page.setRedirect(true);
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
							if (page.getId() == 0) {
								page.setId(Integer.parseInt(content));
							}
							else if (revision != null && revision.getId() == 0) {
								revision.setId(Integer.parseInt(content));
							}
						}
						else if (curElement.equals(TIMESTAMP)) {
							try {
								revision.setDate(dateFormat.parse(content));
							}
							catch (ParseException e) {
								e.printStackTrace();
							}
						}
						else if (curElement.equals(IP) || curElement.equals(USERNAME)) revision.setUser(content);
						else if (curElement.equals(TEXT)) {
							textStart = false;
							revision.setText(content);
						}
						else if (curElement.equals(REVISION)) {
							page.getRevisions().add(revision);
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
		return page;
	}

	/**
	 * Not supported
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public int getPageCount() {
		return pageCount;
	}
}
