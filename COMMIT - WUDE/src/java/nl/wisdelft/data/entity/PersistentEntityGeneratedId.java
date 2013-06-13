/**
 * 
 */
package nl.wisdelft.data.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;

/**
 * @author oosterman
 */
@MappedSuperclass
public abstract class PersistentEntityGeneratedId extends PersistentEntity{
	
	@Override
	@Id
	@Column(name="id")
	@GeneratedValue(generator="gen")
	@GenericGenerator(name="gen",strategy="identity")
	public Long getId() {
		return super.getId();
	}

}
