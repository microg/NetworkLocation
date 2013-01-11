package com.google.android.location.source;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.location.Address;
import android.util.Log;

public class GoogleGeocodeDataSource implements GeocodeDataSource,
		ContentHandler {

	private static final String BASE_URL = "http://maps.googleapis.com/maps/api/geocode/xml?latlng=%lat%,%lon%&sensor=false&region=%region%&language=%lang%";
	private static final String TAG = "GoogleGeocodeDataSource";

	private final Object lock = new Object();
	private List<Address> addrs = null;
	private Address addr = null;
	private Locale locale = null;
	private ArrayList<String> parsingState = null;
	private String compLong = null;
	private String compShort = null;
	private String compType = null;
	private HashMap<Long, List<Address>> cache = new HashMap<Long, List<Address>>();
	private long E9 = 1000000000L;

	@Override
	public void addAdressesToListForLocation(double lat, double lon,
			Locale locale, List<Address> addrs) {
		String urlString = BASE_URL.replace("%lat%", lat + "")
				.replace("%lon%", lon + "")
				.replace("%region%", locale.getCountry())
				.replace("%lang%", locale.getLanguage());
		try {
			long hash = (long) (lat * E9 * E9 + lon * E9);
			synchronized (lock) {
				if (cache.containsKey(hash)) {
				} else {
					URL url = new URL(urlString);
					URLConnection connection = url.openConnection();
					InputStream stream = (InputStream) connection.getContent();
					XMLReader reader = SAXParserFactory.newInstance()
							.newSAXParser().getXMLReader();
					reader.setContentHandler(this);
					parsingState = new ArrayList<String>();
					this.addrs = new ArrayList<Address>();
					this.locale = locale;
					reader.parse(new InputSource(stream));
					cache.put(hash, this.addrs);
					this.addrs = null;
					this.locale = null;
					parsingState = null;
				}
			}
			List<Address> a = cache.get(hash);
			for (Address address : a) {
				addrs.add(address);
			}
		} catch (Exception e) {
			Log.w(TAG, e);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String text = new String(ch, start, length);
		if (parsingState.get(0).equalsIgnoreCase("GeocodeResponse")
				&& parsingState.size() > 1) {
			if (parsingState.get(1).equalsIgnoreCase("status")) {
				if (!text.equalsIgnoreCase("OK")) {
					throw new RuntimeException("GeocodeResponse->status = "
							+ text + " (!=OK)");
				}
			} else if (parsingState.get(1).equalsIgnoreCase("result")
					&& parsingState.size() > 2) {
				if (parsingState.get(2).equalsIgnoreCase("type")) {
				} else if (parsingState.get(2).equalsIgnoreCase(
						"formatted_address")) {
					addr.setAddressLine(addr.getMaxAddressLineIndex() + 1, text);
				} else if (parsingState.get(2).equalsIgnoreCase(
						"address_component")
						&& parsingState.size() > 3) {
					if (parsingState.get(3).equalsIgnoreCase("long_name")) {
						compLong = text;
					} else if (parsingState.get(3).equalsIgnoreCase(
							"short_name")) {
						compShort = text;
					} else if (parsingState.get(3).equalsIgnoreCase("type")) {
						if (compType == null
								|| !text.equalsIgnoreCase("political")) {
							compType = text;
						}
					}
				} else if (parsingState.get(2).equalsIgnoreCase("geometry")
						&& parsingState.size() > 3) {
					if (parsingState.get(3).equalsIgnoreCase("location")
							&& parsingState.size() > 4) {
						if (parsingState.get(4).equalsIgnoreCase("lat")) {
							addr.setLatitude(Double.parseDouble(text));
						} else if (parsingState.get(4).equalsIgnoreCase("lon")) {
							addr.setLongitude(Double.parseDouble(text));
						}
					}
				}
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (parsingState.get(parsingState.size() - 1).equalsIgnoreCase(
				localName)) {
			parsingState.remove(parsingState.size() - 1);
		} else {
			throw new RuntimeException("end of non-open element?! " + localName);
		}
		if (parsingState.size() >= 1
				&& parsingState.get(0).equalsIgnoreCase("GeocodeResponse")) {
			if (parsingState.size() == 1
					&& localName.equalsIgnoreCase("result")) {
				addrs.add(addr);
				addr = null;
			}
			if (parsingState.size() == 2
					&& localName.equalsIgnoreCase("address_component")) {
				if (compType.equalsIgnoreCase("street_number")) {
					addr.setSubThoroughfare(compShort);
				} else if (compType.equalsIgnoreCase("route")) {
					addr.setFeatureName(compShort);
					addr.setThoroughfare(compLong);
				} else if (compType.equalsIgnoreCase("sublocality")) {

				} else if (compType.equalsIgnoreCase("locality")) {
					addr.setLocality(compLong);
				} else if (compType
						.equalsIgnoreCase("administrative_area_level_3")) {

				} else if (compType
						.equalsIgnoreCase("administrative_area_level_1")) {
					addr.setAdminArea(compShort);
				} else if (compType.equalsIgnoreCase("country")) {
					addr.setCountryName(compLong);
					addr.setCountryCode(compShort);
				} else if (compType.equalsIgnoreCase("postal_code")) {
					addr.setPostalCode(compShort);
				}
				compType = null;
				compLong = null;
				compShort = null;
			}
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {

	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {

	}

	@Override
	public void setDocumentLocator(Locator locator) {

	}

	@Override
	public void skippedEntity(String name) throws SAXException {

	}

	@Override
	public void startDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		parsingState.add(localName);
		if (parsingState.get(0).equalsIgnoreCase("GeocodeResponse")) {
			if (localName.equalsIgnoreCase("result")) {
				addr = new Address(locale);
			}
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}

}
