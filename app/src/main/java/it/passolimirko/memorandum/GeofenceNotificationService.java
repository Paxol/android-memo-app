package it.passolimirko.memorandum;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import java.util.List;

import it.passolimirko.memorandum.room.AppDatabase;
import it.passolimirko.memorandum.room.models.Memo;

public class GeofenceNotificationService extends Service implements LocationListener, Observer<List<Memo>> {
    private static final String NOTIFICATION_CHANNEL_ID = "GEOFENCE_CHANNEL_ID";

    LocationManager locationManager;
    private Location location;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // If no location permission, stop
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf(startId);
        }

        // Get location
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null) {
            onLocationFound(location);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationManager.removeUpdates(this);
        onLocationFound(location);
    }

    private void onLocationFound(@NonNull Location location) {
        this.location = location;
        AppDatabase.getInstance(this).memoDao().getActive().observeForever(this);
    }

    @Override
    public void onChanged(List<Memo> memos) {
        if (location == null) return;

        AppDatabase.getInstance(this).memoDao().getActive().removeObserver(this);

        for (Memo m : memos) {
            float[] distance = new float[2];

            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    m.latitude, m.longitude, distance);

            if (distance[0] <= MainActivity.GEOFENCE_RADIUS) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                            getString(R.string.notification_channel_name),
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription(getString(R.string.notification_channel_description));
                    notificationManager.createNotificationChannel(channel);
                }

                Intent intent = new Intent(this, MainActivity.class);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    flags = flags | PendingIntent.FLAG_IMMUTABLE;
                }

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

                notificationManager.notify(m.id, new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(getString(R.string.inside_memo_area))
                        .setContentText(m.title)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build());
            }
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        AppDatabase.getInstance(this).memoDao().getActive().removeObserver(this);
    }
}