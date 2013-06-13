/**
 * 
 */
package nl.wisdelft.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.JSONParser;

/**
 * @author oosterman
 */
public class GooglePlacesParser {
	private static final String PLACESURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
	private static final String APIKEY = "AIzaSyDtKwBqz957oqGmeLyM6umnFFrBbN_QaqE";
	private static final String KEY = "key";
	private static final String LOCATION = "location";
	private static final String RADIUS = "radius";
	private static final String TYPES = "types";
	private static final String dataPath = "/Users/oosterman/Documents/Data/wordlists/places/";
	private static BufferedWriter dataWriter = null;
	private static JSONParser parser = new JSONParser();
	private static String[] types = new String[] { "accounting", "airport", "amusement_park", "aquarium", "art_gallery", "atm", "bakery",
			"bank", "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley", "bus_station", "cafe", "campground", "car_dealer",
			"car_rental", "car_repair", "car_wash", "casino", "cemetery", "church", "city_hall", "clothing_store", "convenience_store",
			"courthouse", "dentist", "department_store", "doctor", "electrician", "electronics_store", "embassy", "establishment", "finance",
			"fire_station", "florist", "food", "funeral_home", "furniture_store", "gas_station", "general_contractor", "grocery_or_supermarket",
			"gym", "hair_care", "hardware_store", "health", "hindu_temple", "home_goods_store", "hospital", "insurance_agency", "jewelry_store",
			"laundry", "lawyer", "library", "liquor_store", "local_government_office", "locksmith", "lodging", "meal_delivery", "meal_takeaway",
			"mosque", "movie_rental", "movie_theater", "moving_company", "museum", "night_club", "painter", "park", "parking", "pet_store",
			"pharmacy", "physiotherapist", "place_of_worship", "plumber", "police", "post_office", "real_estate_agency", "restaurant",
			"roofing_contractor", "rv_park", "school", "shoe_store", "shopping_mall", "spa", "stadium", "storage", "store", "subway_station",
			"synagogue", "taxi_stand", "train_station", "travel_agency", "university", "veterinary_care", "zoo" };

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// start the parsing
		String filename = "PlacesInDelft.txt";
		for (String type : types) {
			storeNearbyPlaces(filename, 52.009507, 4.360515, 5000, type);
			System.out.println(String.format("%s type '%s' parsed", filename, type));
		}
	}

	/**
	 * Returns at most 60 places for the given search parameters
	 * 
	 * @param filename
	 * @param latitude
	 * @param longitude
	 * @param radius
	 * @param types
	 * @param token
	 * @throws Exception
	 */
	public static void storeNearbyPlaces(String filename, double latitude, double longitude, int radius, String types) throws Exception {
		// open the data file for writing(append)
		dataWriter = new BufferedWriter(new FileWriter(new File(dataPath + filename), true));
		// create the base url
		String url = String.format("%s?sensor=false&%s=%s&%s=%s,%s&%s=%s&%s=%s", PLACESURL, KEY, APIKEY, LOCATION, latitude, longitude, RADIUS,
				radius, TYPES, types);
		String nextPageToken = null;
		// get and parse the json
		Map<String, Object> page = getJSON(url);
		// loop through all the results
		while ((nextPageToken = parseResultPage(page)) != null) {
			page = getJSON(url + "&pagetoken=" + nextPageToken);
			// google want us to wait 2 seconds
			Thread.sleep(2000);
		}
		dataWriter.close();
	}

	private static Map<String, Object> getJSON(String url) throws Exception {
		URL u = new URL(url);
		// get contents
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		reader.close();
		// parse the json
		String json = builder.toString();
		try {
			Map<String, Object> parsedJSON = (Map<String, Object>) parser.parse(json);
			return parsedJSON;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(url);
			System.err.println(json);
			return null;
		}
	}

	/**
	 * Parses the JSON and stores the name of the found places
	 * 
	 * @param page
	 * @return the next page token
	 * @throws Exception
	 */
	private static String parseResultPage(Map<String, Object> page) throws Exception {
		if (page == null) return null;
		// get the next page token
		String nextPageToken = (String) page.get("next_page_token");
		// get the results list
		List<Object> results = (List<Object>) page.get("results");
		if (results == null) return null;
		else {
			for (Object result : results) {
				Map<String, Object> place = (Map<String, Object>) result;
				String name = (String) place.get("name");
				dataWriter.write(name);
				dataWriter.newLine();
			}

			return nextPageToken;
		}
	}
}
