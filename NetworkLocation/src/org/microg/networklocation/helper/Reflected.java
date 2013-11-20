package org.microg.networklocation.helper;

import android.content.ContentResolver;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public final class Reflected {
	private Reflected() {
	}

	public static int androidProviderSettingsGlobalGetInt(ContentResolver contentResolver, String name,
														  int defaultValue) {
		try {
			Class clazz = Class.forName("android.provider.Settings$Global");
			Method getInt = clazz.getDeclaredMethod("getInt", ContentResolver.class, String.class, int.class);
			return (Integer) getInt.invoke(null, contentResolver, name, defaultValue);
		} catch (Exception e) {
			Log.w("android.provider.Settings.Global.getInt", e);
			return defaultValue;
		}
	}

	public static void androidLocationLocationMakeComplete(Location location) {
		try {
			Class clazz = Class.forName("android.location.Location");
			Method makeComplete = clazz.getDeclaredMethod("makeComplete");
			makeComplete.invoke(location);
		} catch (Exception e) {
			Log.w("android.location.Location.makeComplete", e);
		}
	}

	public static IBinder androidOsServiceManagerGetService(String service) {
		try {
			Class clazz = Class.forName("android.os.ServiceManager");
			Method makeComplete = clazz.getDeclaredMethod("getService", String.class);
			return (IBinder) makeComplete.invoke(null, service);
		} catch (Exception e) {
			Log.w("android.os.ServiceManager.getService", e);
			return null;
		}
	}
}
