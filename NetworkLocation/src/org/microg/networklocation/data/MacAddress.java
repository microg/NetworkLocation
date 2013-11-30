package org.microg.networklocation.data;

import java.util.Arrays;

public class MacAddress {
	public static final int HEX_RADIX = 16;
	private int[] bytes;

	MacAddress(int[] bytes) {
		if (bytes.length != 6) {
			throw new IllegalArgumentException("A mac address has exactly 6 bytes");
		}
		this.bytes = bytes;
	}

	public static String byteTo2DigitHex(int b) {
		String hex = Integer.toHexString(b);
		if (hex.length() == 1) {
			return "0" + hex;
		}
		return hex;
	}

	public static MacAddress parse(String s) {
		int[] bytes = new int[6];
		if (s.length() == 12) {
			for (int i = 0; i < 6; ++i) {
				bytes[i] = Integer.parseInt(s.substring(i * 2, (i + 1) * 2), HEX_RADIX);
			}
			return new MacAddress(bytes);
		} else if (s.length() == 17) {
			for (int i = 0; i < 6; ++i) {
				bytes[i] = Integer.parseInt(s.substring(i * 3, (i * 3) + 2), HEX_RADIX);
			}
			return new MacAddress(bytes);
		} else {
			String[] splitAtColon = s.split(":");
			if (splitAtColon.length == 6) {
				for (int i = 0; i < 6; ++i) {
					bytes[i] = Integer.parseInt(splitAtColon[i], HEX_RADIX);
				}
				return new MacAddress(bytes);
			}
			String[] splitAtLine = s.split("-");
			if (splitAtLine.length == 6) {
				for (int i = 0; i < 6; ++i) {
					bytes[i] = Integer.parseInt(splitAtLine[i], HEX_RADIX);
				}
				return new MacAddress(bytes);
			}
		}
		throw new IllegalArgumentException("Can't read this string as mac address");
	}

	public int[] getBytes() {
		return bytes;
	}

	@Override
	public String toString() {
		return toString(":");
	}

	public String toString(String byteDivider) {
		StringBuilder sb = new StringBuilder(byteTo2DigitHex(bytes[0]));
		for (int i = 1; i < 6; ++i) {
			sb.append(byteDivider).append(byteTo2DigitHex(bytes[i]));
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MacAddress that = (MacAddress) o;

		if (!Arrays.equals(bytes, that.bytes)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return bytes != null ? Arrays.hashCode(bytes) : 0;
	}
}
