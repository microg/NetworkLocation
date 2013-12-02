package org.microg.networklocation.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOHelper {
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
}
