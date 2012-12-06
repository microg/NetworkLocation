package com.google.android.location;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ConfirmAlertActivity extends Activity {
	private static final String TAG = "LocationConfirmAlertActivity";

	public ConfirmAlertActivity() {
		Log.i(TAG, "new Activty-Object constructed");
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
	}
}
