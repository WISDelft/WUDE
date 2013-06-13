/**
 * 
 */
package nl.wisdelft.data;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * @author oosterman
 */
public class HibernateUtil {
	private static final SessionFactory sessionFactory = buildSessionFactory();

	private static SessionFactory buildSessionFactory() {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			Configuration configuration = new Configuration();
			configuration.configure("hibernate.cfg.xml");
			ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder().applySettings(configuration.getProperties());
			ServiceRegistry registry = serviceRegistryBuilder.buildServiceRegistry();
			return configuration.buildSessionFactory(registry);
		}
		catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * Shorthand function for {@code getSessionFactory().getCurrentSession().beginTransaction() }
	 * @return
	 */
	public static Transaction beginTransaction(){
		return getSessionFactory().getCurrentSession().beginTransaction();
	}
}
