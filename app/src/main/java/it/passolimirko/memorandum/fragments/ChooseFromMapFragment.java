package it.passolimirko.memorandum.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import it.passolimirko.memorandum.MainActivity;
import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentChooseFromMapBinding;

public class ChooseFromMapFragment extends Fragment implements OnMapReadyCallback {
    public static final String TAG = "ModalChooseFromMapFragment";

    public static final String REQUEST_POSITION_FROM_MAP = "positionFromMap";
    public static final String REQUEST_BUNDLE_POSITION_KEY = "position";

    FragmentChooseFromMapBinding binding;
    FragmentActivity activity;

    LatLng initialPosition = null;
    boolean isDefaultInitialPosition = false;

    GoogleMap map;

    Marker selectedPositionMarker = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        if (activity == null) return;

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        if (!(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null)
                initialPosition = new LatLng(location.getLatitude(), location.getLongitude());
        }

        if (initialPosition == null) {
            // No permission or no location found
            // Set location as default
            initialPosition = new LatLng(41.2119788, 12.5261583);
            isDefaultInitialPosition = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChooseFromMapBinding.inflate(inflater, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.choose_map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        binding.btnOk.setOnClickListener((v) -> {
            Bundle result = new Bundle();
            result.putParcelable(REQUEST_BUNDLE_POSITION_KEY, selectedPositionMarker.getPosition());
            getParentFragmentManager().setFragmentResult(REQUEST_POSITION_FROM_MAP, result);
            NavHostFragment.findNavController(ChooseFromMapFragment.this)
                    .popBackStack();
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set the FAB from the activity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();

            mainActivity.setFabOnClickListener(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, isDefaultInitialPosition ? 5.5f : 13));

        map.setOnMapClickListener((latLng) -> {
            if (selectedPositionMarker != null) selectedPositionMarker.remove();

            selectedPositionMarker = map.addMarker(new MarkerOptions().position(latLng));
            binding.btnOk.setEnabled(true);
        });
    }
}
