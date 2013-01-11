package com.google.android.location.source;

import java.io.File;
import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.google.android.location.data.CellLocationData;
import com.google.android.location.database.CellMap;
import com.google.android.location.database.DatabaseHelper;

public class DBFileCellLocationSource implements CellLocationSource {
	private final File dbFile;
	private static final String TAG = "DBFileCellLocationSource";

	public DBFileCellLocationSource(File dbFile) {
		this.dbFile = dbFile;
	}

	@Override
	public void requestCellLocation(int mcc, int mnc, int cid, CellMap cellMap) {
		if (dbFile.exists()) {
			Log.i(TAG, "checking " + dbFile.getAbsolutePath() + " for " + mcc
					+ "/" + mnc + "/" + cid);
			final SQLiteDatabase db = SQLiteDatabase.openDatabase(
					dbFile.getAbsolutePath(), null,
					SQLiteDatabase.OPEN_READONLY
							+ SQLiteDatabase.NO_LOCALIZED_COLLATORS);
			final Cursor c = DatabaseHelper.checkCursor(db.rawQuery(
					"SELECT * FROM cells WHERE mcc=? AND mnc=? AND cellid=?",
					new String[] { mcc + "", mnc + "", cid + "" }));
			if (c != null) {
				while (!c.isLast()) {
					c.moveToNext();
					final Location location = new Location(
							CellLocationData.IDENTIFIER);
					location.setLatitude(c.getDouble(c
							.getColumnIndexOrThrow("lat")));
					location.setLongitude(c.getDouble(c
							.getColumnIndexOrThrow("lon")));
					location.setTime(new Date().getTime());
					cellMap.put(mcc, mnc, cid, location);
				}
				c.close();
			}
			db.close();
		} else {
			Log.w(TAG,
					"could not find input file at " + dbFile.getAbsolutePath());
		}
	}

}
