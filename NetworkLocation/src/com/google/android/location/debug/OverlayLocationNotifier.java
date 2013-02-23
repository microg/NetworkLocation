package com.google.android.location.debug;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.android.location.NetworkLocationService;
import com.google.android.location.R;

public class OverlayLocationNotifier {

	public static boolean locationEqual(final Location l1, final Location l2) {
		return (l1 == null && l2 == null)
				|| (l1 != null && l2 != null
						&& l1.getLatitude() == l2.getLatitude()
						&& l1.getLongitude() == l2.getLongitude() && l1
						.getAccuracy() == l2.getAccuracy());
	}

	private final NotificationManager nm;

	private final NetworkLocationService service;

	public OverlayLocationNotifier(final NetworkLocationService service) {
		this.service = service;
		nm = (NotificationManager) service
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void start() {

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(
				service);
		builder.setAutoCancel(false);
		builder.setContentTitle("NetworkLocation active");
		builder.setContentText("Debug-Mode enabled");
		builder.setPriority(NotificationCompat.PRIORITY_MIN);
		builder.setOngoing(true);
		builder.setSmallIcon(android.R.drawable.stat_notify_sync_noanim);
		builder.setLargeIcon(((BitmapDrawable) service.getResources()
				.getDrawable(R.drawable.app_icon)).getBitmap());
		final RemoteViews v = new RemoteViews("com.google.android.location",
				R.layout.debug_notifiy);
		Intent i = new Intent();
		i.setAction(NetworkLocationService.ACTION_DEBUG_TOGGLE_OVERLAY);
		v.setOnClickPendingIntent(R.id.toggle, PendingIntent.getBroadcast(
				service, service.hashCode() + 1, i, 0));
		i = new Intent();
		i.setAction(NetworkLocationService.ACTION_DEBUG_MOVE_OVERLAY_DOWN);
		v.setOnClickPendingIntent(R.id.down, PendingIntent.getBroadcast(
				service, service.hashCode() + 2, i, 0));
		i = new Intent();
		i.setAction(NetworkLocationService.ACTION_DEBUG_MOVE_OVERLAY_UP);
		v.setOnClickPendingIntent(R.id.up, PendingIntent.getBroadcast(service,
				service.hashCode() + 3, i, 0));
		i = new Intent();
		i.setAction(NetworkLocationService.ACTION_DEBUG_MOVE_OVERLAY_LEFT);
		v.setOnClickPendingIntent(R.id.left, PendingIntent.getBroadcast(
				service, service.hashCode() + 4, i, 0));
		i = new Intent();
		i.setAction(NetworkLocationService.ACTION_DEBUG_MOVE_OVERLAY_RIGHT);
		v.setOnClickPendingIntent(R.id.right, PendingIntent.getBroadcast(
				service, service.hashCode() + 5, i, 0));
		final Location real = service.getRealLocation();
		final Location overlay = service.getOverlayLocation();
		Location l = real;
		if (overlay != null) {
			l = overlay;
			v.setTextViewText(R.id.toggle, "ON");
		} else {
			v.setTextViewText(R.id.toggle, "OFF");
		}
		if (l != null) {
			v.setTextViewText(R.id.loc,
					l.getLatitude() + ", " + l.getLongitude());
		} else {
			v.setTextViewText(R.id.loc, "Unknown...");
		}
		builder.setContent(v);
		nm.notify(service.hashCode(), builder.build());
	}

	public synchronized void stop() {
		nm.cancel(service.hashCode());
	}

}
