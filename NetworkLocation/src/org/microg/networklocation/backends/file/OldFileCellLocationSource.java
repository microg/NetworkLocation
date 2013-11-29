package org.microg.networklocation.backends.file;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.microg.networklocation.MainService;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.database.DatabaseHelper;
import org.microg.networklocation.source.LocationSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OldFileCellLocationSource implements LocationSource<CellSpec> {
	private static final String TAG = "OldFileCellLocationSource";
	private static final String NAME = "Local File Database";
	private static final String DESCRIPTION = "Read cell locations from a database located on the (virtual) sdcard";
	private final File dbFile;

	public OldFileCellLocationSource(final File dbFile) {
		this.dbFile = dbFile;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean isSourceAvailable() {
		return dbFile.exists() && dbFile.canRead();
	}

	@Override
	public Collection<LocationSpec<CellSpec>> retrieveLocation(Collection<CellSpec> specs) {
		List<LocationSpec<CellSpec>> locationSpecs = new ArrayList<LocationSpec<CellSpec>>();
		SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY +
																						SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		for (CellSpec spec : specs) {
			if (MainService.DEBUG) {
				Log.i(TAG, "checking " + dbFile.getAbsolutePath() + " for " + spec);
			}
			Cursor cursor = DatabaseHelper.checkCursor(
					db.rawQuery("SELECT * FROM cells WHERE mcc=? AND mnc=? AND cid=?",
								new String[]{Integer.toString(spec.getMcc()), Integer.toString(spec.getMnc()),
											 Integer.toString(spec.getCid())}));
			if (cursor != null) {
				while (!cursor.isLast()) {
					cursor.moveToNext();
					locationSpecs.add(new LocationSpec<CellSpec>(spec, cursor.getDouble(
							cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE)), cursor.getDouble(
							cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE)), cursor.getDouble(
							cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACCURACY))));
				}
				cursor.close();
			}
		}
		db.close();
		return locationSpecs;
	}
}
