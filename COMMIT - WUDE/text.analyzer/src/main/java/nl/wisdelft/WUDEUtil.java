/**
 * 
 */
package nl.wisdelft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Properties;

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
	
	public static String readFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		}
		finally {
			stream.close();
		}
	}

}
