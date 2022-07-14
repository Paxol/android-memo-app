package it.passolimirko.memorandum.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;

import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentMemoDetailBinding;
import it.passolimirko.memorandum.room.models.Memo;

public class MemoDetailFragment extends Fragment implements OnMapReadyCallback {

    public static final String BUNDLE_MEMO_KEY = "memo";

    private FragmentMemoDetailBinding binding;

    private Memo memo;

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMemoDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null)
            memo = getArguments().getParcelable(BUNDLE_MEMO_KEY);

        if (memo != null) {
            // Mandatory fields first
            binding.memoTitle.setText(memo.title);
            binding.memoExpire.setText(DateFormat.getDateTimeInstance().format(memo.date));
            
            // Set description if exists
            if (memo.content == null || memo.content.isEmpty())
                binding.memoDesc.setVisibility(View.GONE);
            else binding.memoDesc.setText(memo.content);

            // Location => string associated to lat and lng
            if (memo.location != null && !memo.location.isEmpty())
                binding.memoLocation.setText(memo.location);
            else {
                // No location name, check if there is lat and lng
                if (memo.latitude != null && memo.longitude != null) {
                    binding.memoLocation.setText(String.format("%.7f, %.7f", memo.latitude, memo.longitude));
                } else {
                    // No location, hide location icon and text view
                    binding.memoLocation.setVisibility(View.GONE);
                    binding.memoPoiIcon.setVisibility(View.GONE);
                }
            }

            if (memo.latitude != null && memo.longitude != null)
                setupMap();
            else
                // Hide map
                binding.memoMapContainer.setVisibility(View.GONE);

        } else {
            throw new IllegalArgumentException("No Memo given");
        }

        return binding.getRoot();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.memo_map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng position = new LatLng(memo.latitude, memo.longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);

        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13));
    }
}