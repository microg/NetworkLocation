package org.microg.networklocation.v2;

import java.io.ByteArrayOutputStream;

public class CellSpec extends PropSpec {
	private Radio radio;
	private int mcc;
	private int mnc;
	private int lac;
	private int cid;
	private int signal;
	private int ta;
	private int psc;
	private int rssi = Integer.MIN_VALUE;
	private int rscp = Integer.MIN_VALUE;
	private int rsrp = Integer.MIN_VALUE;

	private static byte[] intToByte(int number) {
		byte[] data = new byte[4];
		for (int i = 0; i < 4; ++i) {
			int shift = i << 3; // i * 8
			data[3 - i] = (byte) ((number & (0xff << shift)) >>> shift);
		}
		return data;
	}

	/**
	 * As described here: https://mozilla-ichnaea.readthedocs.org/en/latest/cell.html
	 * @return
	 */
	private int getAsu() {
		switch (radio) {
			case GSM:
				return Math.max(0, Math.min(31, (rssi + 113) / 2));
			case UMTS:
				return Math.max(-5, Math.max(91, rscp + 116));
			case LTE:
				return Math.max(0, Math.min(95, rsrp + 140));
			case CDMA:
				if (rssi >= -75) {
					return 16;
				}
				if (rssi >= -82) {
					return 8;
				}
				if (rssi >= -90) {
					return 4;
				}
				if (rssi >= -95) {
					return 2;
				}
				if (rssi >= -100) {
					return 1;
				}
				return 0;
		}
		return 0;
	}

	public int getCid() {
		return cid;
	}

	@Override
	byte[] getIdentBlob() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			bytes.write(radio.toString().getBytes());
			bytes.write(intToByte(mcc));
			bytes.write(intToByte(mnc));
			bytes.write(intToByte(lac));
			bytes.write(intToByte(cid));
			return bytes.toByteArray();
		} catch (Exception e) {
			return "<error>".getBytes();
		}
	}

	public int getMcc() {
		return mcc;
	}

	public int getMnc() {
		return mnc;
	}
}
