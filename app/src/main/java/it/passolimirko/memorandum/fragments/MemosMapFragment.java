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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentMemosMapBinding;
import it.passolimirko.memorandum.room.AppDatabase;
import it.passolimirko.memorandum.room.models.Memo;

public class MemosMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MemosMapFragment";
    private final Map<LatLng, List<Memo>> memosGroupedByLatLon = new HashMap<>();
    private FragmentMemosMapBinding binding;
    private GoogleMap map;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMemosMapBinding.inflate(inflater, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        return binding.getRoot();
    }

    private void setMemosMap(List<Memo> memos) {
        memosGroupedByLatLon.clear();

        for (Memo m : memos) {
            if (m.latitude == null || m.longitude == null) continue;

            LatLng location = new LatLng(m.latitude, m.longitude);
            List<Memo> memosList = memosGroupedByLatLon.get(location);

            if (memosList != null) {
                memosList.add(m);
            } else {
                memosList = new ArrayList<>();
                memosList.add(m);

                memosGroupedByLatLon.put(location, memosList);
            }
        }
    }

    private void updateMarkers() {
        if (map != null) {
            map.clear();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Map.Entry<LatLng, List<Memo>> entry : memosGroupedByLatLon.entrySet()) {
                LatLng location = entry.getKey();
                List<Memo> memos = entry.getValue();

                MarkerOptions markerOptions = new MarkerOptions();

                if (memos.size() > 1)
                    markerOptions.title(memos.size() + " promemoria");
                else
                    markerOptions.title(memos.get(0).title);

                markerOptions.snippet("Dettagli");
                markerOptions.position(location);

                map.addMarker(markerOptions);
                builder.include(location);
            }

            if (!memosGroupedByLatLon.isEmpty())
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
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

        // Listen to room live data
        AppDatabase.getInstance(requireContext()).memoDao().getActive().observe(getViewLifecycleOwner(),
                memos -> {
                    setMemosMap(memos);
                    updateMarkers();
                });

        map.setOnInfoWindowClickListener(marker -> {
            List<Memo> memosForPosition = memosGroupedByLatLon.get(marker.getPosition());
            if (memosForPosition != null) {
                if (memosForPosition.size() > 1) {
                    // Show the list of memos at the location

                    Bundle data = new Bundle();
                    data.putInt("mode", MemosListFragment.MODE_LIST);
                    data.putParcelableArray("memos", memosForPosition.toArray(new Memo[0]));

                    NavHostFragment.findNavController(MemosMapFragment.this)
                            .navigate(R.id.action_MemosMapFragment_to_MemosListFragment, data);
                } else {
                    // Show the details of the memo

                    Bundle data = new Bundle();
                    data.putParcelable(MemoDetailFragment.BUNDLE_MEMO_KEY, memosForPosition.get(0));

                    NavHostFragment.findNavController(MemosMapFragment.this)
                            .navigate(R.id.action_MemosMapFragment_to_MemoDetailFragment, data);
                }
            }
        });
    }
}