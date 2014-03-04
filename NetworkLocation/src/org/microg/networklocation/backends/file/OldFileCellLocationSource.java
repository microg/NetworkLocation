package org.microg.networklocation.backends.file;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import org.microg.networklocation.MainService;
import org.microg.networklocation.data.CellSpec;
import org.microg.networklocation.data.LocationSpec;
import org.microg.networklocation.source.LocationSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OldFileCellLocationSource implements LocationSource<CellSpec> {
	private static final String TAG = "nlp.OldFileCellLocationSource";
	private static final String NAME = "Local File Database (cells.db)";
	private static final String DESCRIPTION = "Read cell locations from a database located on the (virtual) sdcard";
	private static final String COPYRIGHT = "© unknown\nLicense: unknown";
	private static final String COL_LATITUDE = "lat";
	private static final String COL_LONGITUDE = "lon";
	private final File dbFile = new File(Environment.getExternalStorageDirectory(), ".nogapps/cells.db");

	@Override
	public String getCopyright() {
		return COPYRIGHT;
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
			Cursor cursor = db.rawQuery("SELECT * FROM cells WHERE mcc=? AND mnc=? AND cellid=?",
										new String[]{Integer.toString(spec.getMcc()), Integer.toString(spec.getMnc()),
													 Integer.toString(spec.getCid())});
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					while (!cursor.isLast()) {
						cursor.moveToNext();
						locationSpecs.add(new LocationSpec<CellSpec>(spec, cursor.getDouble(
								cursor.getColumnIndexOrThrow(COL_LATITUDE)), cursor.getDouble(
								cursor.getColumnIndexOrThrow(COL_LONGITUDE)), 5000));
					}
				}
				cursor.close();
			}
		}
		db.close();
		return locationSpecs;
	}
}
