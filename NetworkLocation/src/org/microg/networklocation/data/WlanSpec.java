package org.microg.networklocation.data;

public class WlanSpec implements PropSpec {
	private MacAddress mac;
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
		System.arraycopy(mac.getBytes(), 0, bytes, 4, 6);
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
}
