package org.microg.networklocation.data;

import java.io.ByteArrayOutputStream;

public class CellSpec implements PropSpec {
	private final Radio radio;
	private final int mcc;
	private final int mnc;
	private final int lac;
	private final int cid;
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

	public CellSpec(Radio radio, int mcc, int mnc, int lac, int cid) {
		this.radio = radio;
		this.mcc = mcc;
		this.mnc = mnc;
		this.lac = lac;
		this.cid = cid;
	}

	/**
	 * As described here: https://mozilla-ichnaea.readthedocs.org/en/latest/cell.html
	 *
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
	public byte[] getIdentBlob() {
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

	public int getLac() {
		return lac;
	}

	public int getMcc() {
		return mcc;
	}

	public int getMnc() {
		return mnc;
	}

	public void setPsc(int psc) {
		this.psc = psc;
	}

	@Override
	public String toString() {
		return "CellSpec{" +
			   "radio=" + radio +
			   ", mcc=" + mcc +
			   ", mnc=" + mnc +
			   ", lac=" + lac +
			   ", cid=" + cid +
			   '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CellSpec cellSpec = (CellSpec) o;

		if (cid != cellSpec.cid) {
			return false;
		}
		if (lac != cellSpec.lac) {
			return false;
		}
		if (mcc != cellSpec.mcc) {
			return false;
		}
		if (mnc != cellSpec.mnc) {
			return false;
		}
		if (radio != cellSpec.radio) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = radio.hashCode();
		result = 31 * result + mcc;
		result = 31 * result + mnc;
		result = 31 * result + lac;
		result = 31 * result + cid;
		return result;
	}
}
