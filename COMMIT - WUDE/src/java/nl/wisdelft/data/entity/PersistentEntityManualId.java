/**
 * 
 */
package nl.wisdelft.data.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author oosterman
 *
 */
@MappedSuperclass
public class PersistentEntityManualId extends PersistentEntity{

	@Override
	@Id
	@Column(name="id")
	public Long getId(){
		return super.getId();
	}
}
