package org.microg.networklocation.backends.mapquest;

import android.content.Context;
import android.location.Address;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.microg.networklocation.helper.Networking;
import org.microg.networklocation.source.GeocodeSource;
import org.microg.networklocation.source.OnlineDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NominatimGeocodeSource extends OnlineDataSource implements GeocodeSource {
	private static final String TAG = "nlp.NominatimGeocodeSource";
	private static final String NAME = "MapQuest Nominatim Service";
	private static final String DESCRIPTION = "Reverse geocode using the online service by MapQuest.";
	private static final String COPYRIGHT =
			"Map data © OpenStreetMap contributors\nLicense: ODbL 1.0\nNominatim Search Courtesy of MapQuest";
	private static final String REVERSE_GEOCODE_URL =
			"http://open.mapquestapi.com/nominatim/v1/reverse?format=json&accept-language=%s&lat=%f&lon=%f";
	private static final String WIRE_LATITUDE = "lat";
	private static final String WIRE_LONGITUDE = "lon";
	private static final String WIRE_ADDRESS = "address";
	private static final String WIRE_THOROUGHFARE = "road";
	private static final String WIRE_SUBLOCALITY = "suburb";
	private static final String WIRE_POSTALCODE = "postcode";
	private static final String WIRE_LOCALITY_CITY = "city";
	private static final String WIRE_LOCALITY_TOWN = "town";
	private static final String WIRE_LOCALITY_VILLAGE = "village";
	private static final String WIRE_SUBADMINAREA = "county";
	private static final String WIRE_ADMINAREA = "state";
	private static final String WIRE_COUNTRYNAME = "country";
	private static final String WIRE_COUNTRYCODE = "country_code";
	private final Context context;

	public NominatimGeocodeSource(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public List<Address> getFromLocation(double latitude, double longitude, String sourcePackage, Locale locale) {
		String url = String.format(REVERSE_GEOCODE_URL, locale.getLanguage(), latitude, longitude);
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			Networking.setUserAgentOnConnection(connection, context);
			connection.setDoInput(true);
			InputStream inputStream = connection.getInputStream();
			JSONObject result = new JSONObject(new String(Networking.readStreamToEnd(inputStream)));
			Address address = parseResponse(locale, result);
			if (address != null) {
				List<Address> addresses = new ArrayList<Address>();
				addresses.add(address);
				return addresses;
			}
		} catch (IOException e) {
			Log.w(TAG, e);
		} catch (JSONException e) {
			Log.w(TAG, e);
		}
		return null;
	}

	@Override
	public List<Address> getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude,
											 double upperRightLatitude, double upperRightLongitude,
											 String sourcePackage, Locale locale) {
		return null; //TODO: Implement
	}

	@Override
	public String getName() {
		return NAME;
	}

	private Address parseResponse(Locale locale, JSONObject result) throws JSONException {
		if (!result.has(WIRE_LATITUDE) || !result.has(WIRE_LONGITUDE) || !result.has(WIRE_ADDRESS)) {
			return null;
		}
		Address address = new Address(locale);
		address.setLatitude(result.getDouble(WIRE_LATITUDE));
		address.setLatitude(result.getDouble(WIRE_LONGITUDE));

		int line = 0;
		JSONObject a = result.getJSONObject(WIRE_ADDRESS);

		if (a.has(WIRE_THOROUGHFARE)) {
			address.setAddressLine(line++, a.getString(WIRE_THOROUGHFARE));
			address.setThoroughfare(a.getString(WIRE_THOROUGHFARE));
		}
		if (a.has(WIRE_SUBLOCALITY)) {
			address.setSubLocality(a.getString(WIRE_SUBLOCALITY));
		}
		if (a.has(WIRE_POSTALCODE)) {
			address.setAddressLine(line++, a.getString(WIRE_POSTALCODE));
			address.setPostalCode(a.getString(WIRE_POSTALCODE));
		}
		if (a.has(WIRE_LOCALITY_CITY)) {
			address.setAddressLine(line++, a.getString(WIRE_LOCALITY_CITY));
			address.setLocality(a.getString(WIRE_LOCALITY_CITY));
		} else if (a.has(WIRE_LOCALITY_TOWN)) {
			address.setAddressLine(line++, a.getString(WIRE_LOCALITY_TOWN));
			address.setLocality(a.getString(WIRE_LOCALITY_TOWN));
		} else if (a.has(WIRE_LOCALITY_VILLAGE)) {
			address.setAddressLine(line++, a.getString(WIRE_LOCALITY_VILLAGE));
			address.setLocality(a.getString(WIRE_LOCALITY_VILLAGE));
		}
		if (a.has(WIRE_SUBADMINAREA)) {
			address.setAddressLine(line++, a.getString(WIRE_SUBADMINAREA));
			address.setSubAdminArea(a.getString(WIRE_SUBADMINAREA));
		}
		if (a.has(WIRE_ADMINAREA)) {
			address.setAddressLine(line++, a.getString(WIRE_ADMINAREA));
			address.setAdminArea(a.getString(WIRE_ADMINAREA));
		}
		if (a.has(WIRE_COUNTRYNAME)) {
			address.setAddressLine(line++, a.getString(WIRE_COUNTRYNAME));
			address.setCountryName(a.getString(WIRE_COUNTRYNAME));
		}
		if (a.has(WIRE_COUNTRYCODE)) {
			address.setCountryCode(a.getString(WIRE_COUNTRYCODE));
		}

		return address;
	}
}
