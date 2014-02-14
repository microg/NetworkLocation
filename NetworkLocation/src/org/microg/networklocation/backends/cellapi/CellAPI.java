package org.microg.networklocation.backends.cellapi;

import android.content.Context;
import android.util.Log;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.helper.Networking;
import org.microg.networklocation.source.LocationSource;
import org.microg.networklocation.source.OnlineDataSource;
import org.microg.networklocation.MainService;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

public class CellAPI extends OnlineDataSource implements LocationSource<CellSpec> {
	private static final String TAG = "nlp.CellAPI";

	private static final String NAME = "cell.vodafone.com";
	private static final String DESCRIPTION = "Retrieve cell locations from vodafone.com when online";
	private static final String COPYRIGHT = "© 2007-2014";
	private static final String SERVICE_URL = "https://cellapi.vodafone.com/loc";
	private static final String rid = "nogapps";
	private static final String secret = "ohY7rahb";
	private final Context context;
	static final private java.util.Random random = new java.util.Random();

	public CellAPI(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public boolean isSourceAvailable() {
		return true;
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
	public String getName() {
		return NAME;
	}

	@Override
	public Collection<LocationSpec<CellSpec>> retrieveLocation(Collection<CellSpec> cellSpecs) {
		Collection<LocationSpec<CellSpec>> locationSpecs = new ArrayList<LocationSpec<CellSpec>>();
		for (CellSpec spec : cellSpecs) {
			try {
				if (MainService.DEBUG) Log.d(TAG, "retrieveLocation: checking for " + spec);
				java.util.Map<String,String> map = resolve(
					Integer.toString(spec.getMcc()),
					Integer.toString(spec.getMnc()),
					Integer.toString(spec.getLac()),
					Integer.toString(0),
					Integer.toString(spec.getCid()),
					"-");
				int rcd = Integer.parseInt(map.get("rcd"));
				if (rcd < 2030) { // 2030: based on mcc
					final double lat = Double.parseDouble(map.get("lat"));
					final double lon = Double.parseDouble(map.get("lon"));
					double rad;
					final double altitude = 0.0;
					if (rcd == 2000) // rad included
						rad = Double.parseDouble(map.get("rad"));
					else
						rad = 5000.0;
					locationSpecs.add(new LocationSpec<CellSpec>(spec,
						lat,
						lon,
						altitude,
						rad
						));
				}
				else {
					Log.e(TAG, "retrieveLocation: error=" + rcd);
				}
			}
			catch (Exception e) {
				Log.e(TAG, "retrieveLocation: exception=" + e);
				if (MainService.DEBUG) Log.wtf(TAG, "retrieveLocation: exception=" + e, e);
			}
		}
		if (MainService.DEBUG) Log.d(TAG, "retrieveLocation: locationSpecs=" + locationSpecs);
		return locationSpecs;
	}

	static String generateMD5(String input) throws Exception {
		java.security.MessageDigest digest = java.security.MessageDigest.getInstance("md5");
		return new java.math.BigInteger(1, digest.digest( input.getBytes("utf-8"))).toString(16).toLowerCase();
	}

	static java.util.Map<String,String> decode(String line) throws Exception {
		java.util.StringTokenizer pairs = new java.util.StringTokenizer(line.trim(), "&");
		java.util.Map<String,String> tx = new java.util.HashMap();
		while (pairs.hasMoreTokens()) {
			String pair = (String)pairs.nextToken();
			if (pair.length() > 0) {
				int index = pair.indexOf('=');
				if (index > 0) {
					String name = java.net.URLDecoder.decode(
						pair.substring(0, index), "UTF-8");
					String value = java.net.URLDecoder.decode(
						pair.substring(index+1), "UTF-8");
					tx.put(name, value);
				}
			}
		}
		return tx;
	}

	static String encode(java.util.Map<String,String> map) throws Exception {
		StringBuffer res = new StringBuffer();
		for (String key : map.keySet()) {
			String value = map.get(key);
			if (value != null)
				if (res.length() > 0)
					res.append('&');
				res
					.append(key)
					.append('=')
					.append(java.net.URLEncoder.encode(value, "utf-8"));
		}
		return res.toString();
	}

	static java.util.Map<String,String> resolve(
		String mc,
		String mn,
		String la,
		String ri,
		String ci,
		String uid
		) throws Exception
	{
		String tim = Long.toString(System.currentTimeMillis() / 1000);
		String ver = "1";
		String correlation_id = null;
		// enable if under log4j: correlation_id = org.apache.log4j.MDC.get("correlation-id");
		if (correlation_id == null)
			correlation_id = Long.toString(random.nextLong());
		String aup = generateMD5(
			secret +
			// manually sorted:
			ci +
			la +
			mc +
			mn +
			ri +
			rid +
			tim +
			uid +
			ver +
			"");
		// create the parameter block:
		java.util.Map<String,String> map = new java.util.HashMap<String,String>();
		map.put("mc", mc);
		map.put("mn", mn);
		map.put("ri", ri);
		map.put("la", la);
		map.put("ci", ci);
		if (MainService.DEBUG) Log.d(TAG, "resolve: parameter=" + map);
		java.net.URLConnection conn = new java.net.URL(SERVICE_URL + "?" + encode(map)).openConnection();
		// fill in the meta parameter:
		conn.setRequestProperty("x-vf-csl-aup", aup);
		conn.setRequestProperty("x-vf-csl-rid", rid);
		conn.setRequestProperty("x-vf-csl-tim", tim);
		conn.setRequestProperty("x-vf-csl-uid", uid);
		conn.setRequestProperty("x-vf-csl-ver", ver);
		conn.setRequestProperty("x-correlation-id", correlation_id);
		if (MainService.DEBUG) Log.d(TAG, "resolve: meta=" + conn.getRequestProperties());
		java.io.InputStream input = conn.getInputStream();
		byte[] buffer = new byte[1024];
		String response = "";
		int amount = 0;
		while (amount != -1) {
			amount = input.read(buffer);
			if (amount > 0)
				response += new String(buffer, 0, amount);
		}
		java.util.Map<String,String> ret = decode(response);
		if (MainService.DEBUG) Log.d(TAG, "resolve: response=" + ret);
		return ret;
	}
}
