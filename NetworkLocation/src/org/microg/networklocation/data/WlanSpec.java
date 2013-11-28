package org.microg.networklocation.data;

public class WlanSpec implements PropSpec {
	private MacAddress mac;
	private int channel;
	private int frequency;
	private int signal;

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
}
