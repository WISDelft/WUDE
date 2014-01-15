/**
 * 
 */
package nl.wisdelft.twitter.gatherer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.GZIPOutputStream;

/**
 * @author oosterman
 */
public class DiskUtility {
	static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	static Calendar cal;

	public static boolean writeFile(String file, String content, boolean zipFile) {
		BufferedWriter writer = null;
		boolean success = true;
		try {
			if (zipFile) {
				GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(new File(file + ".zip")));
				writer = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"));
			}
			else {
				writer = new BufferedWriter(new FileWriter(new File(file)));
			}
			writer.write(content);
			writer.flush();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			success = false;
		}
		catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		finally {
			if (writer != null) try {
				writer.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	public static String getOutputDirectory(String baseDirectory, String suffix, boolean addCurrentDate) {
		String dir = baseDirectory;
		// correct for paths without trailing slash
		if (!dir.endsWith("/")) dir += "/";
		// Add the profiles directory
		dir += suffix + "/";
		if (!addCurrentDate) {
			return dir;
		}
		else {
			// add the current date to the directory
			cal = new GregorianCalendar();
			String date = format.format(cal.getTime());
			String dateDir = dir + date + "/";
			return dateDir;
		}
	}

	public static String getOutputFile(String baseDirectory, String suffix, boolean addCurrentDate, long userID, boolean zipFile) {
		String dir = getOutputDirectory(baseDirectory, suffix, addCurrentDate);
		dir += Long.toString(userID) + ".json";
		if (zipFile) dir += ".zip";
		return dir;
	}
}