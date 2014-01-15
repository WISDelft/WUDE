/**
 * 
 */
package nl.wisdelft.wiki.entity;

import java.io.Serializable;

/**
 * @author oosterman
 *
 */
public class RevisionComparison implements Serializable {
	private static final long serialVersionUID = 1L;
	private int previousRevisionId;
	private int currentRevisionId;
	private boolean majorChanges;
	private int nrDeleteDiffsPreviousRevision;
	private int nrInsertsDiffsPreviousRevision;
	private int nrCharactersChanged;
	
	
	public int getPreviousRevisionId() {
		return previousRevisionId;
	}
	public void setPreviousRevisionId(int previousRevisionId) {
		this.previousRevisionId = previousRevisionId;
	}
	public boolean isMajorChanges() {
		return majorChanges;
	}
	public void setMajorChanges(boolean majorChanges) {
		this.majorChanges = majorChanges;
	}
	public int getNrDeleteDiffsPreviousRevision() {
		return nrDeleteDiffsPreviousRevision;
	}
	public void setNrDeleteDiffsPreviousRevision(int nrDeleteDiffsPreviousRevision) {
		this.nrDeleteDiffsPreviousRevision = nrDeleteDiffsPreviousRevision;
	}
	public int getNrInsertsDiffsPreviousRevision() {
		return nrInsertsDiffsPreviousRevision;
	}
	public void setNrInsertsDiffsPreviousRevision(int nrInsertsDiffsPreviousRevision) {
		this.nrInsertsDiffsPreviousRevision = nrInsertsDiffsPreviousRevision;
	}
	public int getCurrentRevisionId() {
		return currentRevisionId;
	}
	public void setCurrentRevisionId(int currentRevisionId) {
		this.currentRevisionId = currentRevisionId;
	}
	public int getNrCharactersChanged() {
		return nrCharactersChanged;
	}
	public void setNrCharactersChanged(int nrCharactersChanged) {
		this.nrCharactersChanged = nrCharactersChanged;
	}
}
