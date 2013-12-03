package org.microg.networklocation.helper;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class Version {
	private static final String TAG = "VersionHelper";

	public static String getMyVersion(Context context) {
		try {
			String pkg = context.getPackageName();
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (Throwable t) {
			Log.w(TAG, t);
			return "?";
		}
	}

	public static String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}

}
