package com.google.android.location;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String TABLE_LOCATION = "locations";
	public static final String COL_VERSION = "version";
	public static final String COL_MAC = "mac";
	public static final String COL_TIME = "time";
	public static final String COL_LATITUDE = "latitude";
	public static final String COL_LONGITUDE = "longitude";
	public static final String COL_ACCURACY = "accuracy";

	private static final String DATABASE_NAME = "location.sqlite";
	private static final int DATABASE_SCHEME_VERSION = 9;

	private boolean databaseOpen = false;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_SCHEME_VERSION);
	}

	private Cursor checkCursor(Cursor c) {
		if (c == null) {
			return null;
		}
		if (c.isAfterLast() || c.isClosed()) {
			c.close();
			return null;
		}
		return c;
	}

	@Override
	public void close() {
		if (isOpen()) {
			getReadableDatabase().close();
		}
	}

	public Map<String, Location> getDatabase() {
		final HashMap<String, Location> map = new HashMap<String, Location>();
		final Cursor c = getLocationCursor();
		if (c == null) {
			return map;
		}
		while (!c.isLast()) {
			c.moveToNext();
			final Location location = getLocationFromCursor(c);
			final String mac = c.getString(c.getColumnIndexOrThrow(COL_MAC));
			map.put(mac, location);
		}
		c.close();
		return map;
	}

	public Location getLocation(String mac) {
		final Cursor c = getLocationCursor(mac);
		if (c == null) {
			return null;
		}
		c.moveToFirst();
		final Location location = getLocationFromCursor(c);
		c.close();
		return location;
	}

	private Cursor getLocationCursor() {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery("SELECT " + COL_MAC + ", " + COL_TIME
				+ ", " + COL_ACCURACY + ", " + COL_LATITUDE + ", "
				+ COL_LONGITUDE + " FROM " + TABLE_LOCATION, null);
		return checkCursor(c);
	}

	private Cursor getLocationCursor(String mac) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery("SELECT " + COL_TIME + ", " + COL_ACCURACY
				+ ", " + COL_LATITUDE + ", " + COL_LONGITUDE + " FROM "
				+ TABLE_LOCATION + " WHERE " + COL_MAC + " = '" + mac
				+ "' LIMIT 1", null);
		return checkCursor(c);
	}

	private Cursor getLocationCursorNext(long latitudeE6, long longitudeE6,
			int num) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery("SELECT " + COL_MAC + ", " + COL_TIME
				+ ", " + COL_ACCURACY + ", " + COL_LATITUDE + ", "
				+ COL_LONGITUDE + " FROM " + TABLE_LOCATION + " ORDER BY (("
				+ COL_LATITUDE + " - " + latitudeE6 + ") * (" + COL_LATITUDE
				+ " - " + latitudeE6 + ") + (" + COL_LONGITUDE + " - "
				+ longitudeE6 + ") * (" + COL_LONGITUDE + " - " + longitudeE6
				+ ")) ASC LIMIT " + num, null);
		return checkCursor(c);
	}

	private Location getLocationFromCursor(Cursor c) {
		final Location location = new Location("network");
		location.setAccuracy(c.getLong(c.getColumnIndexOrThrow(COL_ACCURACY)));
		location.setLatitude(c.getLong(c.getColumnIndexOrThrow(COL_LATITUDE)) / 1E6F);
		location.setLongitude(c.getLong(c.getColumnIndexOrThrow(COL_LONGITUDE)) / 1E6F);
		location.setTime(c.getLong(c.getColumnIndexOrThrow(COL_TIME)));
		return location;
	}

	public Map<String, Location> getNext(double latitude, double longitude,
			int num) {
		final HashMap<String, Location> map = new HashMap<String, Location>();
		final Cursor c = getLocationCursorNext((long) (latitude * 1E6),
				(long) (longitude * 1E6), num);
		if (c == null) {
			return map;
		}
		while (!c.isLast()) {
			c.moveToNext();
			final Location location = getLocationFromCursor(c);
			final String mac = c.getString(c.getColumnIndexOrThrow(COL_MAC));
			map.put(mac, location);
		}
		c.close();
		return map;
	}

	public void insertLocation(String mac, Location location) {
		final SQLiteDatabase db = getWritableDatabase();
		db.execSQL("INSERT OR REPLACE INTO " + TABLE_LOCATION + "("
				+ COL_VERSION + ", " + COL_MAC + ", " + COL_TIME + ", "
				+ COL_LATITUDE + ", " + COL_LONGITUDE + ", " + COL_ACCURACY
				+ ") VALUES (" + DATABASE_SCHEME_VERSION + ", '" + mac + "', "
				+ location.getTime() + ", "
				+ (long) (location.getLatitude() * 1E6) + ", "
				+ (long) (location.getLongitude() * 1E6) + ", "
				+ (long) location.getAccuracy() + ")");
	}

	public boolean isOpen() {
		return databaseOpen;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_LOCATION + "(" + COL_VERSION
				+ " INTEGER, " + COL_MAC + " TEXT PRIMARY KEY, " + COL_TIME
				+ " INTEGER, " + COL_LATITUDE + " INTEGER, " + COL_LONGITUDE
				+ " INTEGER, " + COL_ACCURACY + " INTEGER)");
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		databaseOpen = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < DATABASE_SCHEME_VERSION) {
			// ITS TO OLD - RECREATE DATABASE
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
			onCreate(db);
			oldVersion = newVersion;
		}
	}

}
