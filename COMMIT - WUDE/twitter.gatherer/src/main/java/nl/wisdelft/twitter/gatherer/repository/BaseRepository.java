/**
 * 
 */
package nl.wisdelft.twitter.gatherer.repository;

import nl.wisdelft.twitter.gatherer.entity.PersistentEntity;

/**
 * @author oosterman
 */
public class BaseRepository<T extends PersistentEntity> extends AbstractRepository<T> {

	public BaseRepository(Class<T> type) {
		super(type);
	}

}
