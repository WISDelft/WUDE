/**
 * 
 */
package nl.wisdelft.wiki.entity;

import java.io.Serializable;
import java.util.Date;
import nl.wisdelft.text.Readability;


/**
 * @author oosterman
 *
 */
public class WikiPageStats implements Serializable {
	private static final long serialVersionUID = 1L;
	private String title;
	private int id;
	private int nrRevisions;
	private int minorRevisions;
	private int majorRevisions;
	private int averageNrCharactersChangedPerRevision;
	private int averageDiffDeletePerRevision;
	private int averageDiffInsertPerRevision;
	private int nrContributors;
	private boolean redirect;
	private Date creationDate;
	
	private Readability readabilityLastestRevision;
	private WikiMarkupStats markupStatsLastestRevision;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNrRevisions() {
		return nrRevisions;
	}

	public void setNrRevisions(int nrRevisions) {
		this.nrRevisions = nrRevisions;
	}

	public int getMinorRevisions() {
		return minorRevisions;
	}

	public void setMinorRevisions(int minorRevisions) {
		this.minorRevisions = minorRevisions;
	}

	public int getMajorRevisions() {
		return majorRevisions;
	}

	public void setMajorRevisions(int majorRevisions) {
		this.majorRevisions = majorRevisions;
	}

	public int getAverageNrCharactersChangedPerRevision() {
		return averageNrCharactersChangedPerRevision;
	}

	public void setAverageNrCharactersChangedPerRevision(int averageNrCharactersChangedPerRevision) {
		this.averageNrCharactersChangedPerRevision = averageNrCharactersChangedPerRevision;
	}

	public int getAverageDiffDeletePerRevision() {
		return averageDiffDeletePerRevision;
	}

	public void setAverageDiffDeletePerRevision(int averageDiffDeletePerRevision) {
		this.averageDiffDeletePerRevision = averageDiffDeletePerRevision;
	}

	public int getAverageDiffInsertPerRevision() {
		return averageDiffInsertPerRevision;
	}

	public void setAverageDiffInsertPerRevision(int averageDiffInsertPerRevision) {
		this.averageDiffInsertPerRevision = averageDiffInsertPerRevision;
	}

	public int getNrContributors() {
		return nrContributors;
	}

	public void setNrContributors(int nrContributors) {
		this.nrContributors = nrContributors;
	}

	public boolean isRedirect() {
		return redirect;
	}

	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Readability getReadabilityLastestRevision() {
		return readabilityLastestRevision;
	}

	public void setReadabilityLastestRevision(Readability readabilityLastestRevision) {
		this.readabilityLastestRevision = readabilityLastestRevision;
	}

	public WikiMarkupStats getMarkupStatsLastestRevision() {
		return markupStatsLastestRevision;
	}

	public void setMarkupStatsLastestRevision(WikiMarkupStats markupStatsLastestRevision) {
		this.markupStatsLastestRevision = markupStatsLastestRevision;
	}
}
