/**
 * 
 */
package nl.wisdelft.data.repository;

import nl.wisdelft.data.entity.PersistentEntity;

/**
 * @author oosterman
 */
public class BaseRepository<T extends PersistentEntity> extends AbstractRepository<T> {

	public BaseRepository(Class<T> type) {
		super(type);
	}

}
