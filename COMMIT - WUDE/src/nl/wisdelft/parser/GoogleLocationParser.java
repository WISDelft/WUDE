/**
 * 
 */
package nl.wisdelft.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import nl.wisdelft.data.DataLayer;
import nl.wisdelft.um.GeoLocation;
import org.json.simple.parser.JSONParser;

/**
 * @author oosterman
 */
public class GoogleLocationParser {

	public final static String GEOCODINGURL = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&language=nl";
	private static final String GEO = "latlng";
	private static BufferedWriter dataWriter = null;
	private static JSONParser parser = new JSONParser();
	private static String dataInputPath = "/Users/oosterman/Documents/Data/NLtwitterIDs.txt";
	private static String dataOutputPath = "/Users/oosterman/Documents/Data/usergeodocuments";
	/**
	 * wait four hour to try again with Google
	 */
	private static int timeoutGoogle = 1000 * 60 * 60 * 4;
	/**
	 * wait 15 minutes if we notice that another process is busy
	 */
	private static int timeoutOnDuplicateKey = 1000 * 60 * 15;
	private static int skipUsers = 415;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String path = dataInputPath;
		if (args.length > 0) path = args[0];
		if (args.length > 1) skipUsers = Integer.parseInt(args[1]);
		// check if the inputfile exists
		File input = new File(path);
		if (!input.exists() || !input.isFile()) throw new FileNotFoundException("File '" + path + "' does not exist");
		DataLayer datalayer = new DataLayer();
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line = null;
		int count = 0;
		while ((line = reader.readLine()) != null) {
			if (++count < skipUsers) {
				continue;
			}
			// get the filename
			String name = line.trim();
			long userId = Long.parseLong(name);
			// now get all the geocoded tweets from that user
			List<String> geos = datalayer.getGeolocationFromTweets(userId);
			for (String latlng : geos) {
				boolean store = false;
				// check in DB if we already know the geolocation
				String json = datalayer.getJSONForGeolocation(latlng);
				if (json == null) {
					// retrieve from google
					String url = buildURL(latlng);
					json = getJSON(url);
					store = true;
					System.out.println("Geolocation from Google");
					Thread.sleep(700);
				}
				else {
					// System.out.print(".");
				}
				// parse the json
				Map<String, Object> parsedJson = parseJSON(json);

				// results come from the DB, but are not correct
				if (!store && parsedJson == null) {
					datalayer.removeGeolocation(latlng);
					System.out.println("Incorrect entry removed from DB...");
				}

				// there are results and we should store them
				if (store && parsedJson != null) {
					boolean success = false;
					while (!success) {
						try {
							datalayer.storeGeolocation(latlng, json);
							success = true;
						}
						catch (SQLException ex) {
							if(ex.getMessage().contains("duplicate key")){
								//another process is busy, wait
								System.out.println("Sleeping because another process is busy");
								Thread.sleep(timeoutOnDuplicateKey);
							}
						}
					}
				}
				// process the json
				// GeoLocation geoloc = processParsedJSON(parsedJson);
			}
			System.out.println(count + " users processed");
		}
	}

	private static String buildURL(String geo) {
		// return String.format("%s&%s=%s&%s=%s", GEOCODINGURL, KEY, APIKEY, GEO,
		// geo);
		return String.format("%s&%s=%s", GEOCODINGURL, GEO, geo);
	}

	private static String getJSON(String url) throws Exception {
		URL u = new URL(url);
		// get contents
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		reader.close();
		return builder.toString();
	}

	private static Map<String, Object> parseJSON(String json) throws Exception {
		Map<String, Object> parsedJSON = null;
		try {
			parsedJSON = (Map<String, Object>) parser.parse(json);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(json);
			return null;
		}
		// get status and check if is OK.
		String status = (String) parsedJSON.get("status");
		if ("OK".equals(status)) {
			return parsedJSON;
		}
		else if ("ZERO_RESULTS".equals(status)) {
			return null;
		}
		else if ("OVER_QUERY_LIMIT".equals(status)) {
			System.out.println("Sleeping because this IP is over the Google limit");
			Thread.sleep(timeoutGoogle);
			return null;
		}
		else if ("REQUEST_DENIED".equals(status)) {
			throw new Exception(status);
		}
		else if ("INVALID_REQUEST".equals(status)) {
			throw new Exception(status);
		}
		else {
			throw new Exception("Unsupported google status: " + status);
		}

	}

	/**
	 * Parses the JSON and stores the name of the found places
	 * 
	 * @param page
	 * @throws Exception
	 */
	private static GeoLocation processParsedJSON(Map<String, Object> page) throws Exception {
		if (page == null) return null;

		// get the results list
		List<Object> results = (List<Object>) page.get("results");
		if (results == null || results.size() == 0) return null;

		GeoLocation geo = new GeoLocation();

		// first result is the interesting part
		Map<String, Object> first = (Map<String, Object>) results.get(0);
		// get the pretty print address
		String formattedAddress = (String) first.get("formatted_address");
		geo.addressString = formattedAddress;

		// get the geolocation
		Map<String, Map<String, Double>> geometry = (Map<String, Map<String, Double>>) first.get("geometry");
		Map<String, Double> location = geometry.get("location");
		geo.lat = location.get("lat");
		geo.lng = location.get("lng");

		// get the address components
		List<Map<String, Object>> comps = (List<Map<String, Object>>) first.get("address_components");

		for (Map<String, Object> comp : comps) {
			String longName = (String) comp.get("long_name");
			Object o_type = comp.get("types");
			String type = null;
			// get the type (string or first element from array
			if (o_type instanceof String) type = (String) o_type;
			else {
				List<String> types = (List<String>) o_type;
				if (types != null && types.size() > 0) type = types.get(0);
				else continue;
			}
			geo.addressComponents.add(geo.new AddressComponent(longName, type));
		}
		return geo;
	}
}
