/**
 * 
 */
package nl.wisdelft.data.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * @author oosterman
 *
 */
@MappedSuperclass
public abstract class PersistentEntity {
	private Timestamp lastUpdate;
	private Long id;
	
	@Version
	@Column(name = "lastupdate")
	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	@SuppressWarnings("unused")
	private void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	@Override
	public boolean equals(Object o) {
		if(o==null)
			return false;
		if(this == o)
			return true;
		if (o instanceof PersistentEntity) {
			PersistentEntity other = (PersistentEntity) o;
			return this.id==other.id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Transient
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

}
