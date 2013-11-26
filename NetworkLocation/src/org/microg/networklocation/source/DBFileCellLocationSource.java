package org.microg.networklocation.source;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import org.microg.networklocation.MainService;
import org.microg.networklocation.data.CellLocationData;
import org.microg.networklocation.database.CellMap;
import org.microg.networklocation.database.DatabaseHelper;

import java.io.File;
import java.util.Date;

public class DBFileCellLocationSource implements CellLocationSource {
	private static final String TAG = "DBFileCellLocationSource";
	private static final String NAME = "Local File Database";
	private static final String DESCRIPTION = "Read cell locations from a database located on the (virtual) sdcard";
	private final File dbFile;

	public DBFileCellLocationSource(final File dbFile) {
		this.dbFile = dbFile;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public void requestCellLocation(final int mcc, final int mnc, final int cid, int lac, final CellMap cellMap) {
		if (dbFile.exists()) {
			if (MainService.DEBUG)
				Log.i(TAG, "checking " + dbFile.getAbsolutePath() + " for " + mcc + "/" + mnc + "/" + cid);
			final SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null,
																  SQLiteDatabase.OPEN_READONLY +
																  SQLiteDatabase.NO_LOCALIZED_COLLATORS);
			final Cursor c = DatabaseHelper.checkCursor(
					db.rawQuery("SELECT * FROM cells WHERE mcc=? AND mnc=? AND cellid=?",
								new String[]{mcc + "", mnc + "", cid + ""}));
			if (c != null) {
				while (!c.isLast()) {
					c.moveToNext();
					final Location location = new Location(CellLocationData.IDENTIFIER);
					location.setLatitude(c.getDouble(c.getColumnIndexOrThrow("lat")));
					location.setLongitude(c.getDouble(c.getColumnIndexOrThrow("lon")));
					location.setTime(new Date().getTime());
					cellMap.put(mcc, mnc, cid, location);
				}
				c.close();
			}
			db.close();
		} else {
			if (MainService.DEBUG)
				Log.w(TAG, "could not find input file at " + dbFile.getAbsolutePath());
		}
	}

	@Override
	public boolean isSourceAvailable() {
		return dbFile.exists() && dbFile.canRead();
	}
}
