package org.microg.networklocation.v2;

enum Radio {
	GSM, UMTS, CDMA, LTE;
	private static final String UNKNOWN_RADIO_TYPE = "Unknown radio type!";

	public Radio getPrimaryRadio() {
		switch (this) {
			case GSM:
			case UMTS:
			case LTE:
				return GSM;
			case CDMA:
				return CDMA;
		}
		throw new IllegalArgumentException(UNKNOWN_RADIO_TYPE);
	}

	@Override
	public String toString() {
		switch (this) {
			case GSM:
				return "gsm";
			case UMTS:
				return "umts";
			case CDMA:
				return "cdma";
			case LTE:
				return "lte";
		}
		throw new IllegalArgumentException(UNKNOWN_RADIO_TYPE);
	}
}
