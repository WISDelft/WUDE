/**
 * 
 */
package nl.wisdelft.data.repository;

import java.util.HashSet;
import java.util.Set;
import nl.wisdelft.data.HibernateUtil;
import nl.wisdelft.data.entity.PersistentEntity;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

/**
 * @author oosterman
 */
public abstract class AbstractRepository<T extends PersistentEntity> {
	private SessionFactory factory = HibernateUtil.getSessionFactory();
	private Class<T> type;

	public AbstractRepository(final Class<T> type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	public T findById(final Long id) {
		Criteria c = factory.getCurrentSession().createCriteria(type).add(Restrictions.idEq(id));
		return (T) c.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public Set<T> findAll() {
		Criteria c = factory.getCurrentSession().createCriteria(type);
		return new HashSet<T>(c.list());
	}

	public void saveOrUpdate(final T entity) {
		factory.getCurrentSession().saveOrUpdate(entity);
	}

	public void update(final T entity) {
		factory.getCurrentSession().update(entity);
	}

	public void delete(final T entity) {
		factory.getCurrentSession().delete(entity);
	}

	public void persist(final T entity) {
		factory.getCurrentSession().persist(entity);
	}
	
	public boolean exists(long id){
		return factory.getCurrentSession().get(type, id) == null;
	}
	
	@SuppressWarnings("unchecked")
	public T get(long id){
		return (T)factory.getCurrentSession().get(type, id);
	}
	/**
	 * Checks whether the entity already exists (based on ID) and persists the
	 * entity otherwise
	 * 
	 * @param entity
	 * @return the (persisted) entity
	 */
	public T createIfNotExists(final T entity) {
		Criteria c = getFactory().getCurrentSession().createCriteria(type);
		c.add(Restrictions.idEq(entity.getId()));
		if (c.uniqueResult() == null) {
			persist(entity);
		}
		return entity;
	}

	public SessionFactory getFactory() {
		return factory;
	}

}
