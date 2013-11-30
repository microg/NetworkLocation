package org.microg.networklocation.data;

public class WlanSpec implements PropSpec {
	private final MacAddress mac;
	private int channel;
	private int frequency;
	private int signal;

	public WlanSpec(MacAddress mac) {
		this.mac = mac;
	}

	public WlanSpec(MacAddress mac, int frequency, int signal) {
		this.mac = mac;
		this.frequency = frequency;
		this.signal = signal;
	}

	public WlanSpec(MacAddress mac, int channel) {
		this.mac = mac;
		this.channel = channel;
	}

	@Override
	public byte[] getIdentBlob() {
		byte[] bytes = new byte[10];
		bytes[0] = 'w';
		bytes[1] = 'i';
		bytes[2] = 'f';
		bytes[3] = 'i';
		for(int i = 0; i < 6; ++i) {
			bytes[i+4] = (byte) mac.getBytes()[i];
		}
		return bytes;
	}

	public MacAddress getMac() {
		return mac;
	}

	@Override
	public String toString() {
		return "WlanSpec{" +
			   "mac=" + mac +
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

		WlanSpec wlanSpec = (WlanSpec) o;

		if (mac != null ? !mac.equals(wlanSpec.mac) : wlanSpec.mac != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return mac != null ? mac.hashCode() : 0;
	}
}
