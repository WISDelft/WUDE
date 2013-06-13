/**
 * 
 */
package nl.wisdelft;

import java.io.File;
import java.net.URL;

/**
 * @author oosterman
 */
public final class Utility {

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
