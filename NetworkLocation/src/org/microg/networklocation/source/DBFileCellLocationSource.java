package org.microg.networklocation.source;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.microg.networklocation.MainService;
import org.microg.networklocation.database.DatabaseHelper;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBFileCellLocationSource implements LocationSource<CellSpec> {
	private static final String TAG = "DBFileCellLocationSource";
	private static final String NAME = "Local File Database";
	private static final String DESCRIPTION = "Read cell locations from a database located on the (virtual) sdcard";
	private final File dbFile;

	public DBFileCellLocationSource(final File dbFile) {
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
			final Cursor c = DatabaseHelper.checkCursor(
					db.rawQuery("SELECT * FROM cells WHERE mcc=? AND mnc=? AND lac=? AND cid=?",
								new String[]{Integer.toString(spec.getMcc()), Integer.toString(spec.getMnc()),
											 Integer.toString(spec.getLac()), Integer.toString(spec.getCid())}));
			if (c != null) {
				while (!c.isLast()) {
					c.moveToNext();
					locationSpecs.add(new LocationSpec<CellSpec>(spec, c.getDouble(
							c.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE)), c.getDouble(
							c.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE)), c.getDouble(
							c.getColumnIndexOrThrow(DatabaseHelper.COL_ACCURACY))));
				}
				c.close();
			}
		}
		db.close();
		return locationSpecs;
	}
}
