/**
 * 
 */
package nl.wisdelft.wiki.entity;

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * @author oosterman
 */
public class Revisions implements Serializable,Iterable<Revision> {

	private static final long serialVersionUID = 1L;

	private TreeSet<Revision> revisions;

	public Revisions() {
		revisions = new TreeSet<Revision>();
	}

	public boolean add(Revision rev) {
		return revisions.add(rev);
	}

	public int size() {
		return revisions.size();
	}

	/**
	 * Gets the latest revision
	 * 
	 * @return the latest revision or null if there are no revisions
	 */
	public Revision getLatestRevision() {
		int size = size();
		if (size == 0) return null;
		else return revisions.last();
	}

	/**
	 * Gets the first revision (When the page was created)
	 * 
	 * @return the first revision or null if there are no revisions
	 */
	public Revision getFirstRevision() {
		int size = size();
		if (size == 0) return null;
		else return revisions.first();
	}


	@Override
	public Iterator<Revision> iterator() {
		return revisions.iterator();
	}
}
