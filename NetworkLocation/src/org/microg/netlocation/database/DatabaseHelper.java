package org.microg.netlocation.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String COL_ACCURACY = "accuracy";
	public static final String COL_ALTITUDE = "altitude";
	public static final String COL_CID = "cid";
	public static final String COL_LATITUDE = "latitude";
	public static final String COL_LONGITUDE = "longitude";
	public static final String COL_MAC = "mac";
	public static final String COL_MCC = "mcc";
	public static final String COL_MNC = "mnc";
	public static final String COL_TIME = "time";

	public static final String COL_VERSION = "version";
	private static final String DATABASE_NAME = "location.sqlite";
	public static final int DEFAULT_ACCURACY = 5000;
	private static DatabaseHelper instance;

	private static final int LATEST_DATABASE_SCHEME_VERSION = 11;
	private static final int NO_ALTITUDE_DATABASE_SCHEME_VERSION = 10;
	// CELLS: version, mcc, mnc, cid, time, latitude, longitude
	public static final String TABLE_CELLS = "cells";
	// WLANS: version, mac, time, latitude, longitude, accuracy, altitude
	public static final String TABLE_WLANS = "wlans";
	public static final String TABLE_WLANS_OLD = "locations";

	private static final int WLAN_ONLY_DATABASE_SCHEME_VERSION = 9;

	public static Cursor checkCursor(final Cursor c) {
		if (c == null) {
			return null;
		}
		if (c.isAfterLast() || c.isClosed() || c.getCount() <= 0) {
			c.close();
			return null;
		}
		return c;
	}

	public static DatabaseHelper getInstance(final Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context);
		}
		return instance;
	}

	private Thread autoClose;

	private boolean databaseOpen = false;

	private boolean newRequest;

	public DatabaseHelper(final Context context) {
		super(context, DATABASE_NAME, null, LATEST_DATABASE_SCHEME_VERSION);
		startAutoCloseThread();
		newRequest = true;
	}

	@Override
	public void close() {
		if (isOpen()) {
			getReadableDatabase().close();
			databaseOpen = false;
		}
	}

	private void createCellsTable(final SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_CELLS + "(" + COL_VERSION + " INTEGER, " + COL_MCC + " INTEGER, " + COL_MNC +
				   " INTEGER, " + COL_CID + " INTEGER, " + COL_TIME + " INTEGER, " + COL_LATITUDE + " INTEGER, " +
				   COL_LONGITUDE + " INTEGER)");
	}

	private void createWlanTable(final SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_WLANS + "(" + COL_VERSION + " INTEGER, " + COL_MAC + " TEXT PRIMARY KEY, " +
				   COL_TIME + " INTEGER, " + COL_LATITUDE + " INTEGER, " + COL_LONGITUDE + " INTEGER, " + COL_ACCURACY +
				   " INTEGER, " + COL_ALTITUDE + " INTEGER)");
	}

	private Cursor getLocationCursorGsmCell(final int mcc, final int mnc, final int cid) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery(
				"SELECT " + COL_TIME + ", " + COL_LATITUDE + ", " + COL_LONGITUDE + " FROM " + TABLE_CELLS + " WHERE " +
				COL_MCC + "=" + mcc + " AND mnc=" + mnc + " AND cid=" + cid + " LIMIT 1", null);
		return checkCursor(c);
	}

	private Cursor getLocationCursorNextWlan(final long latitudeE6, final long longitudeE6, final int num) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c =
				db.rawQuery("SELECT " + COL_MAC + ", " + COL_TIME + ", " + COL_ACCURACY + ", " + COL_LATITUDE + ", " +
							COL_LONGITUDE + " FROM " + TABLE_WLANS + " ORDER BY ((" + COL_LATITUDE + " - " +
							latitudeE6 + ") * (" +
							COL_LATITUDE + " - " + latitudeE6 + ") + (" + COL_LONGITUDE + " - " + longitudeE6 +
							") * (" +
							COL_LONGITUDE + " - " + longitudeE6 + ")) ASC LIMIT " + num, null);
		return checkCursor(c);
	}

	private Cursor getLocationCursorWlan(final String mac) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor c = db.rawQuery(
				"SELECT " + COL_TIME + ", " + COL_ACCURACY + ", " + COL_LATITUDE + ", " + COL_LONGITUDE + " FROM " +
				TABLE_WLANS + " WHERE " + COL_MAC + " LIKE '" + mac + "' LIMIT 1", null);
		return checkCursor(c);
	}

	private Location getLocationFromCursor(final Cursor c) {
		final Location location = new Location("direct");
		final int accuracyIndex = c.getColumnIndex(COL_ACCURACY);
		if (accuracyIndex != -1) {
			location.setAccuracy(c.getLong(accuracyIndex));
		} else {
			location.setAccuracy(DEFAULT_ACCURACY);
		}
		final int altitudeIndex = c.getColumnIndex(COL_ALTITUDE);
		if (altitudeIndex != -1) {
			final long alt = c.getLong(altitudeIndex);
			if (alt > 0) {
				location.setAltitude(c.getLong(altitudeIndex));
			}
		}
		location.setLatitude(c.getLong(c.getColumnIndexOrThrow(COL_LATITUDE)) / 1E6F);
		location.setLongitude(c.getLong(c.getColumnIndexOrThrow(COL_LONGITUDE)) / 1E6F);
		location.setTime(c.getLong(c.getColumnIndexOrThrow(COL_TIME)));
		return location;
	}

	public Location getLocationGsmCell(final int mcc, final int mnc, final int cid) {
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

	public Location getLocationWlan(final String mac) {
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

	public Map<String, Location> getNextWlan(final double latitude, final double longitude, final int num) {
		newRequest = true;
		final HashMap<String, Location> map = new HashMap<String, Location>();
		final Cursor c = getLocationCursorNextWlan((long) (latitude * 1E6), (long) (longitude * 1E6), num);
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

	public void insertGsmCellLocation(final int mcc, final int mnc, final int cid, final Location location) {
		newRequest = true;
		final SQLiteDatabase db = getWritableDatabase();
		db.execSQL(
				"INSERT OR REPLACE INTO " + TABLE_CELLS + "(" + COL_VERSION + ", " + COL_MCC + ", " + COL_MNC + ", " +
				COL_CID + ", " + COL_TIME + ", " + COL_LATITUDE + ", " + COL_LONGITUDE + ") VALUES ('" +
				LATEST_DATABASE_SCHEME_VERSION + "', '" + mcc + "', '" + mnc + "', '" + cid + "', '" +
				location.getTime() + "', '" + (long) (location.getLatitude() * 1E6) + "', '" +
				(long) (location.getLongitude() * 1E6) + "')");
	}

	public void insertWlanLocation(final String mac, final Location location) {
		newRequest = true;
		final SQLiteDatabase db = getWritableDatabase();
		db.execSQL(
				"INSERT OR REPLACE INTO " + TABLE_WLANS + "(" + COL_VERSION + ", " + COL_MAC + ", " + COL_TIME + ", " +
				COL_LATITUDE + ", " + COL_LONGITUDE + ", " + COL_ACCURACY + ", " + COL_ALTITUDE + ") VALUES (" +
				LATEST_DATABASE_SCHEME_VERSION + ", '" + mac + "', " + location.getTime() + ", " +
				(long) (location.getLatitude() * 1E6) + ", " + (long) (location.getLongitude() * 1E6) + ", " +
				(long) location.getAccuracy() + ", " + (long) location.getAltitude() + ")");
	}

	public boolean isOpen() {
		return databaseOpen;
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		createWlanTable(db);
		createCellsTable(db);
	}

	@Override
	public void onOpen(final SQLiteDatabase db) {
		super.onOpen(db);
		databaseOpen = true;
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, int oldVersion, final int newVersion) {
		if (oldVersion < WLAN_ONLY_DATABASE_SCHEME_VERSION) {
			// ITS TO OLD - RECREATE DATABASE
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_WLANS_OLD);
			createWlanTable(db);
			createCellsTable(db);
			oldVersion = LATEST_DATABASE_SCHEME_VERSION;
		}
		if (oldVersion < NO_ALTITUDE_DATABASE_SCHEME_VERSION) {
			// RENAME OLD TABLE
			db.execSQL("ALTER TABLE " + TABLE_WLANS_OLD + " RENAME TO " + TABLE_WLANS);
			createCellsTable(db);
			oldVersion = NO_ALTITUDE_DATABASE_SCHEME_VERSION;
		}
		if (oldVersion == NO_ALTITUDE_DATABASE_SCHEME_VERSION) {
			db.execSQL("ALTER TABLE " + TABLE_WLANS + " ADD COLUMN " + COL_ALTITUDE + " INTEGER");
			oldVersion = LATEST_DATABASE_SCHEME_VERSION;
		}
	}

	private Thread startAutoCloseThread() {
		if (autoClose == null || !autoClose.isAlive()) {
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
