package it.passolimirko.memorandum;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;

import it.passolimirko.memorandum.databinding.ActivityMainBinding;
import it.passolimirko.memorandum.room.AppDatabase;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private View.OnClickListener fabClickListener;

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

        checkForLocationPermissions();
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

    private void checkForLocationPermissions() {
        String fineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        int permissionCheck = ContextCompat.checkSelfPermission(this, fineLocation);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, fineLocation)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(android.R.string.ok, (d, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                        })
                        .setNegativeButton(R.string.cancel, (d, i) -> {})
                        .create()
                        .show();

                ActivityCompat.requestPermissions(this, new String[]{fineLocation}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{fineLocation}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }

    }

    public Snackbar makeSnackbar(int resId, int length) {
        return Snackbar.make(binding.getRoot(), resId, length);
    }
}