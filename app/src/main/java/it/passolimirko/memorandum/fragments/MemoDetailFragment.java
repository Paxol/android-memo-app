package it.passolimirko.memorandum.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.Date;

import it.passolimirko.memorandum.MainActivity;
import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentMemoDetailBinding;
import it.passolimirko.memorandum.room.AppDatabase;
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

            // Update the UI based on the current status
            setMemoStatus(memo.status);

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

        binding.memoBtnDelete.setOnClickListener((v) -> {
            Context ctx = getContext();
            if (ctx == null) return;

            // Create and show confirmation dialog
            new AlertDialog.Builder(ctx)
                    .setTitle(getString(R.string.title_confirm_delete, memo.title))
                    .setMessage(R.string.desc_confirm_delete)
                    .setPositiveButton(android.R.string.yes, (d, i) -> {
                        AppDatabase.getInstance(ctx).memoDao().delete(memo).addListener(() -> {
                            // Go back after we delete the memo
                            NavHostFragment.findNavController(this).popBackStack();

                            // Show a snackbar with undo button
                            if (getActivity() instanceof MainActivity) {
                                final MainActivity act = (MainActivity) getActivity();
                                act.makeSnackbar(R.string.deleted, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.cancel, (v2) -> {
                                            AppDatabase.getInstance(ctx).memoDao().insertAll(memo).addListener(
                                                    // Show restored message
                                                    () -> {
                                                        act.makeSnackbar(R.string.restored, Snackbar.LENGTH_SHORT).show();
                                                    },
                                                    ContextCompat.getMainExecutor(ctx)
                                            );
                                        }).show();
                            } else {
                                // Should not happen
                                // Show toast
                                Toast.makeText(ctx, R.string.deleted, Toast.LENGTH_SHORT).show();
                            }
                        }, ContextCompat.getMainExecutor(ctx));
                    })
                    .setNegativeButton(R.string.cancel, (d, i) -> {
                    })
                    .create()
                    .show();
        });

        binding.memoBtnComplete.setOnClickListener((v) -> {
            Context ctx = getContext();
            if (ctx == null) return;

            setMemoStatus(memo.status == Memo.STATUS_ACTIVE
                    ? Memo.STATUS_COMPLETED
                    : Memo.STATUS_ACTIVE);

            new Thread(
                    () -> AppDatabase.getInstance(ctx).memoDao().updateStatus(memo.id, memo.status)
            ).start();
        });

        return binding.getRoot();
    }

    private void setMemoStatus(int newStatus) {
        switch (newStatus) {
            case Memo.STATUS_ACTIVE:
                memo.status = Memo.STATUS_ACTIVE;

                binding.memoStatus.setText(memo.date.getTime() > new Date().getTime() ? R.string.active : R.string.expired);
                binding.memoBtnComplete.setText(R.string.completed);
                break;
            case Memo.STATUS_COMPLETED:
                memo.status = Memo.STATUS_COMPLETED;

                binding.memoStatus.setText(R.string.completed);
                binding.memoBtnComplete.setText(R.string.not_completed);
                break;
            default:
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.memo_map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setFabOnClickListener(null);
        }
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