package it.passolimirko.memorandum;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.passolimirko.memorandum.databinding.ActivityMainBinding;
import it.passolimirko.memorandum.room.AppDatabase;
import it.passolimirko.memorandum.room.models.Memo;

public class MainActivity extends AppCompatActivity {
    public static final float GEOFENCE_RADIUS = 500;
    private static final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 10;
    private static final int MIN_MILLIS_INSIDE_GEOFENCE = 30000;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private View.OnClickListener fabClickListener;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply Android 12 DynamicColors
        DynamicColors.applyToActivityIfAvailable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnLongClickListener((v) -> {
            AppDatabase.getInstance(MainActivity.this).memoDao().deleteAll();
            return true;
        });

        boolean requested = checkForLocationPermissions();
        if (!requested) setupGeofencing();
    }

    private void setupGeofencing() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkForLocationPermissions();
            return;
        }

        geofencingClient = LocationServices.getGeofencingClient(this);

        AppDatabase.getInstance(this).memoDao().getActive().observe(this, memos -> {
            List<Geofence> geofences = new ArrayList<>();

            for (Memo m : memos) {
                if (m.latitude != null && m.longitude != null) {
                    long currentTime = new Date().getTime();

                    geofences.add(new Geofence.Builder()
                            .setRequestId(String.valueOf(m.id))
                            .setCircularRegion(m.latitude, m.longitude, GEOFENCE_RADIUS)
                            .setExpirationDuration(m.date.getTime() - currentTime)
                            .setLoiteringDelay(MIN_MILLIS_INSIDE_GEOFENCE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                            .build());
                }
            }

            if (geofences.size() > 0)
                geofencingClient.addGeofences(getGeofencingRequest(geofences), getGeofencePendingIntent())
                        .addOnFailureListener(this, (exception) -> {
                            // Failed to add geofences
                            Snackbar.make(binding.getRoot(), R.string.error_occurred, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.details, (view) -> new AlertDialog.Builder(this)
                                            .setTitle(R.string.error_occurred)
                                            .setMessage(R.string.error_no_geofence_notifications)
                                            .setPositiveButton(R.string.retry, (dialogInterface, i) -> setupGeofencing())
                                            .setNegativeButton(R.string.cancel, null)
                                            .show())
                                    .show();
                        });
        });
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // Trigger when the user is inside a geofence for a certain time
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofences);

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags | PendingIntent.FLAG_IMMUTABLE;
        }

        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                flags);
        return geofencePendingIntent;
    }

    @Override
    protected void onResume() {
        super.onResume();

        binding.fab.setOnClickListener(fabClickListener);
    }

    public void setFabOnClickListener(View.OnClickListener listener) {
        if (binding != null) {
            binding.fab.setOnClickListener(listener);

            if (listener == null)
                binding.fab.hide();
            else
                binding.fab.show();
        }

        fabClickListener = listener;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private boolean checkForLocationPermissions() {
        boolean permissionRequested = false;

        String fineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        int finePermissionCheck = ContextCompat.checkSelfPermission(this, fineLocation);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            String backgroundLocation = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
            int backgroundPermissionCheck = ContextCompat.checkSelfPermission(this, backgroundLocation);

            if (finePermissionCheck != PackageManager.PERMISSION_GRANTED || backgroundPermissionCheck != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, fineLocation)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, backgroundLocation)) {
                    showLocationPermissionDialog();

                    ActivityCompat.requestPermissions(this, new String[]{fineLocation, backgroundLocation}, PERMISSIONS_REQUEST_ACCESS_LOCATION);
                    permissionRequested = true;
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, new String[]{fineLocation, backgroundLocation}, PERMISSIONS_REQUEST_ACCESS_LOCATION);
                    permissionRequested = true;
                }
            }
        } else {
            if (finePermissionCheck != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, fineLocation)) {
                    showLocationPermissionDialog();

                    ActivityCompat.requestPermissions(this, new String[]{fineLocation}, PERMISSIONS_REQUEST_ACCESS_LOCATION);
                    permissionRequested = true;
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, new String[]{fineLocation}, PERMISSIONS_REQUEST_ACCESS_LOCATION);
                    permissionRequested = true;
                }
            }
        }

        return permissionRequested;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
                setupGeofencing();
        }
    }

    private void showLocationPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_location_permission)
                .setMessage(R.string.text_location_permission)
                .setPositiveButton(android.R.string.ok, (d, i) -> {
                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_LOCATION);
                })
                .setNegativeButton(R.string.cancel, (d, i) -> {
                })
                .create()
                .show();
    }

    public Snackbar makeSnackbar(int resId, int length) {
        return Snackbar.make(binding.getRoot(), resId, length);
    }
}