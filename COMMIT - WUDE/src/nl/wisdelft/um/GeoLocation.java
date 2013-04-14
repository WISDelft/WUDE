/**
 * 
 */
package nl.wisdelft.um;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oosterman
 */
public class GeoLocation {
	public String addressString;
	public double lat;
	public double lng;
	public List<AddressComponent> addressComponents = new ArrayList<GeoLocation.AddressComponent>();

	@Override
	public String toString() {
		return addressString;
	}

	public class AddressComponent {
		String name;
		String type;

		public AddressComponent(String name, String type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String toString() {
			return "type: " + type+", name: "+name;
		}
	}
}
