/**
 * 
 */
package nl.wisdelft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.hibernate.cfg.Configuration;

/**
 * @author oosterman
 */
public class WUDEUtil {
	private static Properties properties;
	private static final String WUDE_PROPERTIES_FILE = "wude.properties";

	public enum Property {
		TWITTERCONFIGFILE
	}

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

	private WUDEUtil() {}

	private static Properties getProperties() {
		return properties;
	}

	public static String getProperty(Property prop) {
		return getProperty(prop.name());
	}

	public static String getProperty(Property prop, String defaultValue) {
		return getProperty(prop.name(), defaultValue);
	}

	protected static String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	protected static String getProperty(String key, String defaultValue) {
		return getProperties().getProperty(key, defaultValue);
	}

	private static String resourceFolder = null;

	public static String getResourceFolder() {
		if (resourceFolder == null) {
			URL url = ClassLoader.getSystemResource("wude.properties");
			if (url != null) {
				File resource = null;
				try {
					resource = new File(url.toURI());
				}
				catch (Exception ex) {}
				if (resource != null) {
					resourceFolder = resource.getParentFile().getPath();
				}
			}
		}
		return resourceFolder;
	}

	public static String getResourceFilePath(String resourceName) {
		return getResourceFolder() + "/" + resourceName;
	}

}
