/**
 * 
 */
package nl.wisdelft.data.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * @author oosterman
 *
 */
@Entity
@Table(name = "twitterlistinfo")
public class TwitterListInfo extends PersistentEntityManualId implements Serializable {
	private static final long serialVersionUID = 1L;
	private TwitterList list;
	private boolean parsedSubscribers;
	private boolean parsedMembers;

	protected TwitterListInfo() {}

	public TwitterListInfo(TwitterList list) {
		setList(list);
	}

	@OneToOne
	@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
	public TwitterList getList() {
		return list;
	}

	public void setList(TwitterList list) {
		this.list = list;
		setId(list.getId());
	}

	public boolean isParsedSubscribers() {
		return parsedSubscribers;
	}

	public void setParsedSubscribers(boolean parsedSubscribers) {
		this.parsedSubscribers = parsedSubscribers;
	}

	public boolean isParsedMembers() {
		return parsedMembers;
	}

	public void setParsedMembers(boolean parsedMembers) {
		this.parsedMembers = parsedMembers;
	}


}
