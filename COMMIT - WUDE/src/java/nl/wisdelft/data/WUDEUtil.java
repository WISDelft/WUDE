/**
 * 
 */
package nl.wisdelft.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.hibernate.cfg.Configuration;

/**
 * @author oosterman
 */
public class WUDEUtil {
	private static Properties properties;
	private static final String WUDE_PROPERTIES_FILE = "wude.properties";
	
	public enum Property {TWITTERCONFIGFILE}
	
	static {
		properties = new Properties();
		InputStream in = ClassLoader.getSystemResourceAsStream(WUDE_PROPERTIES_FILE);
		try {
			properties.load(in);
			in.close();
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	private WUDEUtil(){}
	
	private static Properties getProperties(){
		return properties;
	}
	
	public static String getProperty(Property prop){
		return getProperty(prop.name());
	}
	
	public static String getProperty(Property prop, String defaultValue){
		return getProperty(prop.name(),defaultValue);
	}
	
	protected static String getProperty(String key){
		return getProperties().getProperty(key);
	}
	
	protected static String getProperty(String key, String defaultValue){
		return getProperties().getProperty(key, defaultValue);
	}
	
	
	
}
