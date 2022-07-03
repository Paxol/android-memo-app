package it.passolimirko.memorandum;

import android.os.Bundle;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import it.passolimirko.memorandum.databinding.ActivityMainBinding;
import it.passolimirko.memorandum.room.AppDatabase;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        binding.fab.setOnClickListener(fabClickListener);
    }

    public void setFabOnClickListener(View.OnClickListener listener) {
        if (binding != null)
            binding.fab.setOnClickListener(listener);

        fabClickListener = listener;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}