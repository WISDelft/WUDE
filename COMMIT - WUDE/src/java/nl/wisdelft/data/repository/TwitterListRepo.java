/**
 * 
 */
package nl.wisdelft.data.repository;

import nl.wisdelft.data.entity.TwitterList;

/**
 * @author oosterman
 */
public class TwitterListRepo extends AbstractRepository<TwitterList> {

	/**
	 * @param factory
	 */
	public TwitterListRepo() {
		super(TwitterList.class);
	}

}
