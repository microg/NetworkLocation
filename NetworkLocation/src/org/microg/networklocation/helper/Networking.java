package org.microg.networklocation.helper;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class Networking {
	private static final String USER_AGENT_FIELD = "User-Agent";
	private static final String USER_AGENT_FORMAT = "NetworkLocation/%s (Linux; Android %s)";
	private static String USER_AGENT;

	public static String getUserAgent(String nlpVersion, String androidVersion) {
		if (USER_AGENT == null) {
			USER_AGENT = String.format(USER_AGENT_FORMAT, nlpVersion, androidVersion);
		}
		return USER_AGENT;
	}

	public static String getUserAgent(Context context) {
		if (USER_AGENT == null) {
			return getUserAgent(Version.getMyVersion(context), Version.getAndroidVersion());
		}
		return USER_AGENT;
	}

	public static byte[] readStreamToEnd(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (is != null) {
			byte[] buff = new byte[1024];
			while (true) {
				int nb = is.read(buff);
				if (nb < 0) {
					break;
				}
				bos.write(buff, 0, nb);
			}
			is.close();
		}
		return bos.toByteArray();
	}

	public static void setUserAgentOnConnection(URLConnection connection, Context context) {
		connection.setRequestProperty(USER_AGENT_FIELD, getUserAgent(context));
	}
}
