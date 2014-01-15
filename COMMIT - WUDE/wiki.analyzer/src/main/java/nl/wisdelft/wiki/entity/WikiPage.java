/**
 * 
 */
package nl.wisdelft.wiki.entity;

import java.io.Serializable;

/**
 * @author oosterman
 */
public class WikiPage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String title;
	private int id;
	private Revisions revisions = new Revisions();
	private boolean redirect = false;
	private String redirectTo = null;

	public WikiPage(String title) {
		if (title == null || title.trim().length() == 0) throw new NullPointerException("Page title cannot be null or empty");
		this.title = title;
	}

	public Revision getLatestRevision() {
		return revisions.getLatestRevision();
	}

	public Revision getFirstRevision(){
		return revisions.getFirstRevision();
	}

	@Override
	public String toString() {
		String output = "Page: " + title + ", Revisions: " + revisions.size() + "\n===========\n ";
		output += revisions.toString();
		return output;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof WikiPage)) return false;
		else {
			return ((WikiPage) other).title.equals(this.title);
		}
	}

	@Override
	public int hashCode() {
		return title.hashCode();
	}

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

	public boolean isRedirect() {
		return redirect;
	}

	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}

	public String getRedirectTo() {
		return redirectTo;
	}

	public void setRedirectTo(String redirectTo) {
		this.redirectTo = redirectTo;
	}

	public Revisions getRevisions() {
		return revisions;
	}

}
