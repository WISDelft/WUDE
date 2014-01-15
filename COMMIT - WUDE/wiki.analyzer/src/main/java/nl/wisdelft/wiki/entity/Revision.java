/**
 * 
 */
package nl.wisdelft.wiki.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import nl.wisdelft.text.OpenNLP.Language;
import nl.wisdelft.wiki.entity.diff_match_patch.Diff;
import nl.wisdelft.wiki.entity.diff_match_patch.Operation;

/**
 * @author oosterman
 */
public class Revision implements Serializable, Comparable<Revision> {
	private static final long serialVersionUID = 1L;

	public static diff_match_patch diff = new diff_match_patch();

	private int id;
	private Date date;
	private String user;
	private String text;
	private Language lang;
	private int lengthInBytes;
	/**
	 * Whether the revision was tagged as minor revision
	 */
	private boolean minorRevision = false;

	public Revision(Language lang) {
		this.lang = lang;
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

	public RevisionComparison compareToRevision(Revision prevRevision) {
		// setup the storage
		RevisionComparison comp = new RevisionComparison();
		List<Diff> diffs;
		int changeInLengthPreviousRevision = 0;
		int nrDeleteDiffsPreviousRevision = 0;
		int nrInsertDiffsPreviousRevision = 0;
		boolean majorChanges = false;
		// perform the comparison
		if (prevRevision == null) {
			diffs = diff.diff_main("", this.getText());
			changeInLengthPreviousRevision = getText().length();
			majorChanges = true;
		}
		else {
			diffs = diff.diff_main(prevRevision.getText(), this.getText());
			changeInLengthPreviousRevision = getText().length() - prevRevision.getText().length();
		}
		for (Diff diff : diffs) {
			if (diff.operation == Operation.DELETE) nrDeleteDiffsPreviousRevision++;
			else if (diff.operation == Operation.INSERT) nrInsertDiffsPreviousRevision++;
		}
		if (prevRevision != null) {
			// heuristics: if length increase or decrease > 200 a paragraph has been
			// added/removed or if many (small) edits have been performed (> 10 diffs)
			// it is major
			if (changeInLengthPreviousRevision > 200 || changeInLengthPreviousRevision < -200
					|| nrDeleteDiffsPreviousRevision + nrInsertDiffsPreviousRevision > 10) majorChanges = true;
		}

		comp.setMajorChanges(majorChanges);
		comp.setCurrentRevisionId(getId());
		if (prevRevision != null) comp.setPreviousRevisionId(prevRevision.getId());
		comp.setNrDeleteDiffsPreviousRevision(nrDeleteDiffsPreviousRevision);
		comp.setNrInsertsDiffsPreviousRevision(nrInsertDiffsPreviousRevision);
		comp.setNrCharactersChanged(changeInLengthPreviousRevision);
		return comp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Language getLang() {
		return lang;
	}

	public void setLang(Language lang) {
		this.lang = lang;
	}

	public boolean isMinorRevision() {
		return minorRevision;
	}

	public void setMinorRevision(boolean minorRevision) {
		this.minorRevision = minorRevision;
	}

	@Override
	public int compareTo(Revision other) {
		if (other == null) return 1;
		return this.getId() - this.getId();
	}

	public int getLengthInBytes() {
		return lengthInBytes;
	}

	public void setLengthInBytes(int lengthInBytes) {
		this.lengthInBytes = lengthInBytes;
	}
}
