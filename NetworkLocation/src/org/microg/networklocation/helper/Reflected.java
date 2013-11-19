package org.microg.networklocation.helper;

import android.content.ContentResolver;
import android.location.Location;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public final class Reflected {
	private static final String METHOD_LOCATION_MAKE_COMPLETE = "android.location.Location.makeComplete";
	private static final String METHOD_SETTINGS_GLOBAL_GET_INT = "android.provider.Settings.Global.getInt";

	private Reflected() {
	}

	public static int androidProviderSettingsGlobalGetInt(ContentResolver contentResolver, String name,
														  int defaultValue) {
		try {
			Class clazz = Class.forName("android.provider.Settings$Global");
			Method getInt = clazz.getDeclaredMethod("getInt", ContentResolver.class, String.class, int.class);
			return (Integer) getInt.invoke(null, contentResolver, name, defaultValue);
		} catch (ClassNotFoundException e) {
			Log.w(METHOD_SETTINGS_GLOBAL_GET_INT, e);
			return defaultValue;
		} catch (NoSuchMethodException e) {
			Log.w(METHOD_SETTINGS_GLOBAL_GET_INT, e);
			return defaultValue;
		} catch (InvocationTargetException e) {
			Log.w(METHOD_SETTINGS_GLOBAL_GET_INT, e);
			return defaultValue;
		} catch (IllegalAccessException e) {
			Log.w(METHOD_SETTINGS_GLOBAL_GET_INT, e);
			return defaultValue;
		}
	}

	public static void androidLocationLocationMakeComplete(Location location) {
		try {
			Class clazz = Class.forName("android.location.Location");
			Method makeComplete = clazz.getDeclaredMethod("makeComplete");
			makeComplete.invoke(location);
		} catch (ClassNotFoundException e) {
			Log.w(METHOD_LOCATION_MAKE_COMPLETE, e);
		} catch (NoSuchMethodException e) {
			Log.w(METHOD_LOCATION_MAKE_COMPLETE, e);
		} catch (InvocationTargetException e) {
			Log.w(METHOD_LOCATION_MAKE_COMPLETE, e);
		} catch (IllegalAccessException e) {
			Log.w(METHOD_LOCATION_MAKE_COMPLETE, e);
		}
	}
}
