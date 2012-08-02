package com.google.android.location;

import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class DatabaseHelper extends SQLiteOpenHelper {

	// WLANS: version, mac, time, latitude, longitude, accuracy
	public static final String TABLE_WLANS = "wlans";
	public static final String TABLE_WLANS_OLD = "locations";
	public static final String COL_VERSION = "version";
	public static final String COL_MAC = "mac";
	public static final String COL_TIME = "time";
	public static final String COL_LATITUDE = "latitude";
	public static final String COL_LONGITUDE = "longitude";
	public static final String COL_ACCURACY = "accuracy";

	// CELLS: version, mcc, mnc, cid, time, latitude, longitude
	public static final String TABLE_CELLS = "cells";
	public static final String COL_MCC = "mcc";
	public static final String COL_MNC = "mnc";
	public static final String COL_CID = "cid";

	private static final String DATABASE_NAME = "location.sqlite";
	private static final int WLAN_ONLY_DATABASE_SCHEME_VERSION = 9;
	private static final int DATABASE_SCHEME_VERSION = 10;
	public static final int DEFAULT_ACCURACY = 500;

	public static Cursor checkCursor(Cursor c) {
		if (c == null) {
			return null;
		}
		if (c.isAfterLast() || c.isClosed() || c.getCount() <= 0) {
			c.close();
			return null;
		}
		return c;
	}

	private boolean databaseOpen = false;
	private boolean newRequest;

	private Thread autoClose;

	private static DatabaseHelper instance;

	public static DatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context);
		}
		return instance;
	}

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_SCHEME_VERSION);
		newRequest = false;
	}

	@Override
	public void close() {
		if (isOpen()) {
			getReadableDatabase().close();
			databaseOpen = false;
		}
	}

	private void createCellsTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_CELLS + "(" + COL_VERSION
				+ " INTEGER, " + COL_MCC + " INTEGER, " + COL_MNC
				+ " INTEGER, " + COL_CID + " INTEGER, " + COL_TIME
				+ " INTEGER, " + COL_LATITUDE + " INTEGER, " + COL_LONGITUDE
				+ " INTEGER)");
	}

	private void createWlanTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_WLANS + "(" + COL_VERSION
				+ " INTEGER, " + COL_MAC + " TEXT PRIMARY KEY, " + COL_TIME
				+ " INTEGER, " + COL_LATITUDE + " INTEGER, " + COL_LONGITUDE
				+ " INTEGER, " + COL_ACCURACY + " INTEGER)");
	}

	private Cursor getLocationCursorGsmCell(int mcc, int mnc, int cid) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery("SELECT " + COL_TIME + ", " + COL_LATITUDE
				+ ", " + COL_LONGITUDE + " FROM " + TABLE_CELLS + " WHERE "
				+ COL_MCC + "=" + mcc + " AND mnc=" + mnc + " AND cid=" + cid
				+ " LIMIT 1", null);
		return checkCursor(c);
	}

	private Cursor getLocationCursorNextWlan(long latitudeE6, long longitudeE6,
			int num) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery("SELECT " + COL_MAC + ", " + COL_TIME
				+ ", " + COL_ACCURACY + ", " + COL_LATITUDE + ", "
				+ COL_LONGITUDE + " FROM " + TABLE_WLANS + " ORDER BY (("
				+ COL_LATITUDE + " - " + latitudeE6 + ") * (" + COL_LATITUDE
				+ " - " + latitudeE6 + ") + (" + COL_LONGITUDE + " - "
				+ longitudeE6 + ") * (" + COL_LONGITUDE + " - " + longitudeE6
				+ ")) ASC LIMIT " + num, null);
		return checkCursor(c);
	}

	private Cursor getLocationCursorWlan() {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery("SELECT " + COL_MAC + ", " + COL_TIME
				+ ", " + COL_ACCURACY + ", " + COL_LATITUDE + ", "
				+ COL_LONGITUDE + " FROM " + TABLE_WLANS, null);
		return checkCursor(c);
	}

	private Cursor getLocationCursorWlan(String mac) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery("SELECT " + COL_TIME + ", " + COL_ACCURACY
				+ ", " + COL_LATITUDE + ", " + COL_LONGITUDE + " FROM "
				+ TABLE_WLANS + " WHERE " + COL_MAC + " LIKE '" + mac
				+ "' LIMIT 1", null);
		return checkCursor(c);
	}

	private Location getLocationFromCursor(Cursor c) {
		final Location location = new Location("direct");
		final int accuracyIndex = c.getColumnIndex(COL_ACCURACY);
		if (accuracyIndex != -1) {
			location.setAccuracy(c.getLong(accuracyIndex));
		} else {
			location.setAccuracy(DEFAULT_ACCURACY);
		}
		location.setLatitude(c.getLong(c.getColumnIndexOrThrow(COL_LATITUDE)) / 1E6F);
		location.setLongitude(c.getLong(c.getColumnIndexOrThrow(COL_LONGITUDE)) / 1E6F);
		location.setTime(c.getLong(c.getColumnIndexOrThrow(COL_TIME)));
		return location;
	}

	public Location getLocationGsmCell(int mcc, int mnc, int cid) {
		newRequest = true;
		final Cursor c = getLocationCursorGsmCell(mcc, mnc, cid);
		if (c == null) {
			return null;
		}
		c.moveToFirst();
		final Location location = getLocationFromCursor(c);
		c.close();
		return location;
	}

	public Location getLocationWlan(String mac) {
		newRequest = true;
		final Cursor c = getLocationCursorWlan(mac);
		if (c == null) {
			return null;
		}
		c.moveToFirst();
		final Location location = getLocationFromCursor(c);
		c.close();
		return location;
	}

	public Map<String, Location> getNextWlan(double latitude, double longitude,
			int num) {
		newRequest = true;
		final HashMap<String, Location> map = new HashMap<String, Location>();
		final Cursor c = getLocationCursorNextWlan((long) (latitude * 1E6),
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

	public Map<String, Location> getWlanTable() {
		newRequest = true;
		final HashMap<String, Location> map = new HashMap<String, Location>();
		final Cursor c = getLocationCursorWlan();
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

	public void insertGsmCellLocation(int mcc, int mnc, int cid,
			Location location) {
		newRequest = true;
		final SQLiteDatabase db = getWritableDatabase();
		db.execSQL("INSERT OR REPLACE INTO " + TABLE_CELLS + "(" + COL_VERSION
				+ ", " + COL_MCC + ", " + COL_MNC + ", " + COL_CID + ", "
				+ COL_TIME + ", " + COL_LATITUDE + ", " + COL_LONGITUDE
				+ ") VALUES ('" + DATABASE_SCHEME_VERSION + "', '" + mcc + "', '"
				+ mnc + "', '" + cid + "', '" + location.getTime() + "', '"
				+ (long) (location.getLatitude() * 1E6) + "', '"
				+ (long) (location.getLongitude() * 1E6) + "')");
	}

	public void insertWlanLocation(String mac, Location location) {
		newRequest = true;
		final SQLiteDatabase db = getWritableDatabase();
		db.execSQL("INSERT OR REPLACE INTO " + TABLE_WLANS + "(" + COL_VERSION
				+ ", " + COL_MAC + ", " + COL_TIME + ", " + COL_LATITUDE + ", "
				+ COL_LONGITUDE + ", " + COL_ACCURACY + ") VALUES ("
				+ DATABASE_SCHEME_VERSION + ", '" + mac + "', "
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
		createWlanTable(db);
		createCellsTable(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		databaseOpen = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < WLAN_ONLY_DATABASE_SCHEME_VERSION) {
			// ITS TO OLD - RECREATE DATABASE
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_WLANS_OLD);
			createWlanTable(db);
			createCellsTable(db);
			oldVersion = DATABASE_SCHEME_VERSION;
		} else if (oldVersion < DATABASE_SCHEME_VERSION) {
			// RENAME OLD TABLE
			db.execSQL("ALTER TABLE " + TABLE_WLANS_OLD + " RENAME TO "
					+ TABLE_WLANS);
			createCellsTable(db);
			oldVersion = DATABASE_SCHEME_VERSION;
		}
	}

	public Thread startAutoCloseThread() {
		if (autoClose == null || autoClose.getState() == State.TERMINATED) {
			autoClose = new Thread(new Runnable() {

				@Override
				public void run() {
					while (autoClose != null) {
						try {
							synchronized (autoClose) {
								autoClose.wait(30000); // wait 30sec
							}
						} catch (final InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (newRequest) {
							newRequest = false;
						} else if (isOpen()) {
							close();
						}
					}
				}
			});
		}
		if (!autoClose.isAlive()) {
			autoClose.start();
		}
		return autoClose;
	}

}
