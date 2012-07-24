package com.google.android.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ConfirmAlertActivity extends Activity {
	private static final String TAG = ConfirmAlertActivity.class.getName();

	public ConfirmAlertActivity() {
		Log.i(TAG, "new Service-Object constructed");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(TAG, "onBind");
		Log.i(TAG, "> Action: " + intent.getAction());
		Log.i(TAG, "> DataString: " + intent.getDataString());
		Log.i(TAG, "> Extras: " + intent.getExtras());
		for (final String string : intent.getExtras().keySet()) {
			Log.i(TAG, "> Extra " + string + ": "
					+ intent.getExtras().getString(string));
		}
		Log.i(TAG, "> Intent: " + intent);
		super.onNewIntent(intent);
	}
}
