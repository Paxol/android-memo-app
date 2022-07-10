package it.passolimirko.memorandum.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import it.passolimirko.memorandum.databinding.FragmentChooseFromMapBinding;

public class ChooseFromMapFragment extends Fragment {
    public static final String TAG = "ModalChooseFromMapFragment";

    FragmentChooseFromMapBinding binding;
    Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        context = getContext();
//        if (context == null) return;
//
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // No location permission, set camera to default
//
//            return;
//        } else {
//
//        }
//
//        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
//        if (location != null)
//        {
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChooseFromMapBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar bar = getActionBar();
        if (bar != null) bar.hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        ActionBar bar = getActionBar();
        if (bar != null) bar.show();
    }

    private ActionBar getActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            return activity.getSupportActionBar();
        }
        return null;
    }
}
