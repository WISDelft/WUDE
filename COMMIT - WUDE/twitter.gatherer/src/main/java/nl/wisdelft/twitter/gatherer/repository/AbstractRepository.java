/**
 * 
 */
package nl.wisdelft.twitter.gatherer.repository;

import java.util.HashSet;
import java.util.Set;
import nl.wisdelft.twitter.gatherer.HibernateUtil;
import nl.wisdelft.twitter.gatherer.entity.PersistentEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * @author oosterman
 */
public abstract class AbstractRepository<T extends PersistentEntity> {
	private Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	private Class<T> type;

	public AbstractRepository(final Class<T> type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	public T findById(final Long id) {
		Criteria c = session.createCriteria(type).add(Restrictions.idEq(id));
		return (T) c.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public Set<T> findAll() {
		Criteria c = session.createCriteria(type);
		return new HashSet<T>(c.list());
	}

	public void saveOrUpdate(final T entity) {
		session.saveOrUpdate(entity);
	}

	public void update(final T entity) {
		session.update(entity);
	}

	public void delete(final T entity) {
		session.delete(entity);
	}

	public void persist(final T entity) {
		session.persist(entity);
	}

	public boolean exists(long id) {
		return session.get(type, id) == null;
	}

	@SuppressWarnings("unchecked")
	public T get(long id) {
		return (T) session.get(type, id);
	}

	/**
	 * Checks whether the entity already exists (based on ID) and persists the
	 * entity otherwise
	 * 
	 * @param entity
	 * @return the (persisted) entity
	 */
	public T createIfNotExists(final T entity) {
		Criteria c = session.createCriteria(type);
		c.add(Restrictions.idEq(entity.getId()));
		if (c.uniqueResult() == null) {
			persist(entity);
		}
		return entity;
	}

}
