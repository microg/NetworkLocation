package org.microg.networklocation.backends.file;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;

import java.io.File;

public class CellLocationFile {
	public static final String BY_SPEC = "mcc=? AND mnc=? AND lac=? AND cid=?";
	public static final int DEFAULT_OPENCELLID_MIN_ACCURACY = 20000;
	public static final int DEFAULT_OPENCELLID_MAX_ACCURACY = 5000;
	public static final int DEFAULT_OPENCELLID_BASE_ACCURACY = 50000;
	private static final String TABLE_CELLS = "cells";
	private static final String COL_LATITUDE = "latitude";
	private static final String COL_LONGITUDE = "longitude";
	private static final String COL_ALTITUDE = "altitude";
	private static final String COL_ACCURACY = "accuracy";
	private static final String COL_MCC = "mcc";
	private static final String COL_MNC = "mnc";
	private static final String COL_LAC = "lac";
	private static final String COL_CID = "cid";
	private final File file;
	private SQLiteDatabase database;

	public CellLocationFile(File file) {
		this.file = file;
	}

	private void assertDatabaseOpen() {
		if (database == null) {
			throw new IllegalArgumentException("You need to open the file first!");
		}
	}

	public void close() {
		if (database != null) {
			database.close();
			database = null;
		}
	}

	private boolean codeMatches(CellSpec cellSpec, int mcc, int mnc) {
		return ((mcc < 0) || (cellSpec.getMcc() == mcc)) && (mnc < 0 || cellSpec.getMnc() == mnc);
	}

	public boolean exists() {
		return file.exists() && file.canRead();
	}

	private String[] getBySpecArgs(CellSpec spec) {
		return new String[]{Integer.toString(spec.getMcc()), Integer.toString(spec.getMnc()),
							Integer.toString(spec.getLac()), Integer.toString(spec.getCid())};
	}

	private ContentValues getContentValues(LocationSpec<CellSpec> spec) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(COL_ACCURACY, spec.getAccuracy());
		contentValues.put(COL_ALTITUDE, spec.getAltitude());
		contentValues.put(COL_LATITUDE, spec.getLatitude());
		contentValues.put(COL_LONGITUDE, spec.getLongitude());
		return contentValues;
	}

	public LocationSpec<CellSpec> getLocation(CellSpec spec) {
		assertDatabaseOpen();
		Cursor cursor =
				database.query(TABLE_CELLS, new String[]{COL_LATITUDE, COL_LONGITUDE, COL_ALTITUDE, COL_ACCURACY},
							   BY_SPEC, getBySpecArgs(spec), null, null, null);
		if (cursor != null) {
			try {
				if (cursor.getCount() > 0) {
					while (!cursor.isLast()) {
						cursor.moveToNext();
						return new LocationSpec<CellSpec>(spec,
														  cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE)),
														  cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE)),
														  cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ALTITUDE)),
														  cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ACCURACY)));
					}
				}
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public String getPath() {
		return file.getAbsolutePath();
	}

	private void insert(LocationSpec<CellSpec> spec) {
		ContentValues contentValues = getContentValues(spec);
		contentValues.put(COL_CID, spec.getSource().getCid());
		contentValues.put(COL_LAC, spec.getSource().getLac());
		contentValues.put(COL_MCC, spec.getSource().getMcc());
		contentValues.put(COL_MNC, spec.getSource().getMnc());
		database.insert(TABLE_CELLS, null, contentValues);
	}

	public void open() {
		if (database == null) {
			database = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		}
	}

	public void putLocation(LocationSpec<CellSpec> spec) {
		assertDatabaseOpen();
		if (getLocation(spec.getSource()) == null) {
			insert(spec);
		} else {
			update(spec);
		}
	}

	private void update(LocationSpec<CellSpec> spec) {
		database.update(TABLE_CELLS, getContentValues(spec), BY_SPEC, getBySpecArgs(spec.getSource()));
	}
}
