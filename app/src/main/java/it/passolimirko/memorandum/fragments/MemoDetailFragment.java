package it.passolimirko.memorandum.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMemoDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null)
            memo = getArguments().getParcelable(BUNDLE_MEMO_KEY);

        if (memo != null) {
            binding.memoTitle.setText(memo.title);
            binding.memoDesc.setText(memo.content);
            binding.memoExpire.setText(DateFormat.getDateTimeInstance().format(memo.date));
            binding.memoLocation.setText(memo.latitude + " " + memo.longitude);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.memo_map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        return binding.getRoot();
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