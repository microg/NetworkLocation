package org.microg.networklocation.database;

import java.util.Arrays;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Log;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.data.PropSpec;
import org.microg.networklocation.MainService;

public class LocationDatabase {
	private static final String TAG = "nlp.LocationDatabase";
	private static final String FILE_NAME = "v2loc.db";
	private static final int DB_VERSION = 1;
	private static final String COL_IDENT = "ident";
	private static final String COL_LATITUDE = "latitude";
	private static final String COL_LONGITUDE = "longitude";
	private static final String COL_ALTITUDE = "altitude";
	private static final String COL_ACCURACY = "accuracy";
	private static final String COL_BOOLS = "bools";
	private static final String TABLE_LOCATION = "locations";
	private static final String CREATE_TABLE =
			"CREATE TABLE " + TABLE_LOCATION + "(" + COL_IDENT + " BLOB PRIMARY KEY, " + COL_LATITUDE + " REAL, " +
			COL_LONGITUDE + " REAL, " + COL_ALTITUDE + " REAL, " + COL_ACCURACY + " REAL, " + COL_BOOLS + " INTEGER)";
	private static final String INSERT_INTO =
			"INSERT INTO " + TABLE_LOCATION + "(" + COL_IDENT + "," + COL_LATITUDE + "," + COL_LONGITUDE + "," +
			COL_ALTITUDE + "," + COL_ACCURACY + "," + COL_BOOLS + ") VALUES(?,?,?,?,?,?)";
	private static final String[] DEFAULT_QUERY_SELECT =
			{COL_LATITUDE, COL_LONGITUDE, COL_ALTITUDE, COL_ACCURACY, COL_BOOLS};
	private OpenHelper openHelper;
	private boolean enabled = false;

	public LocationDatabase(Context context) {
		openHelper = new OpenHelper(context);
	}

	public <T extends PropSpec> LocationSpec<T> get(T propSpec) {
		LocationSpec<T> locationSpec = get(propSpec.getIdentBlob());
		if (locationSpec != null) {
			locationSpec.setSource(propSpec);
		}
		return locationSpec;
	}

	public void enable() {
		this.enabled = true;
	}

	private <T extends PropSpec> LocationSpec<T> get(final byte[] identBlob) {
		LocationSpec<T> locationSpec = null;
		if (enabled) {
			final SQLiteDatabase db = openHelper.getReadableDatabase();
			Cursor cursor = db.queryWithFactory(new SQLiteDatabase.CursorFactory() {
				@Override
				public Cursor newCursor(SQLiteDatabase database, SQLiteCursorDriver sqLiteCursorDriver, String s,
										SQLiteQuery sqLiteQuery) {
					sqLiteQuery.bindBlob(1, identBlob);
					return new SQLiteCursor(db, sqLiteCursorDriver, s, sqLiteQuery);
				}
			}, false, TABLE_LOCATION, DEFAULT_QUERY_SELECT, COL_IDENT + "=?", null, null, null, null, null);
			try {
				if (cursor.moveToNext()) {
					double latitude = cursor.getDouble(0);
					double longitude = cursor.getDouble(1);
					double altitude = cursor.getDouble(2);
					double accuracy = cursor.getDouble(3);
					int bools = cursor.getInt(4);
					locationSpec = new LocationSpec<T>(latitude, longitude, accuracy, altitude, bools);
				}
				if (cursor.moveToNext()) {
					Log.e(TAG, "Result not unique");
				}
			}
			finally {
				cursor.close();
			}
		}
		if (MainService.DEBUG) Log.d(TAG, "retrieved identBlob=" + Arrays.toString(identBlob) + ", locationSpec=" + locationSpec);
		return locationSpec;
	}

	private <T extends PropSpec> void insert(byte[] identBlob, LocationSpec<T> locationSpec) {
		SQLiteStatement statement = openHelper.getWritableDatabase().compileStatement(INSERT_INTO);
		statement.bindBlob(1, identBlob);
		statement.bindDouble(2, locationSpec.getLatitude());
		statement.bindDouble(3, locationSpec.getLongitude());
		statement.bindDouble(4, locationSpec.getAltitude());
		statement.bindDouble(5, locationSpec.getAccuracy());
		statement.bindLong(6, locationSpec.getBools());
		try {
			statement.executeInsert();
			if (MainService.DEBUG) Log.d(TAG, "inserted identBlob=" + Arrays.toString(identBlob) + ", locationSpec=" + locationSpec);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		finally {
			statement.close();
		}
	}

	public <T extends PropSpec> void put(LocationSpec<T> locationSpec) {
		// TODO update if exists!
		insert(locationSpec.getSource().getIdentBlob(), locationSpec);
	}

	private class OpenHelper extends SQLiteOpenHelper {

		private OpenHelper(Context context) {
			super(context, FILE_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL(CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int i, int i2) {
			throw new RuntimeException("Cannot upgrade database from " + i + " to " + i2);
		}
	}
}
