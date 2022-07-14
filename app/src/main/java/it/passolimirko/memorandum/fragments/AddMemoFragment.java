package it.passolimirko.memorandum.fragments;

import android.annotation.SuppressLint;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import it.passolimirko.memorandum.MainActivity;
import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentAddMemoBinding;
import it.passolimirko.memorandum.room.AppDatabase;
import it.passolimirko.memorandum.room.models.Memo;
import it.passolimirko.memorandum.utils.AddressFormatter;
import it.passolimirko.memorandum.utils.Geocoding;

public class AddMemoFragment extends Fragment {

    FragmentAddMemoBinding binding;

    private long selectedExpiration;
    private LatLng selectedPosition;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddMemoBinding.inflate(inflater, container, false);

        binding.btnEditDateTime.setOnClickListener((v) -> pickExpiration());

        binding.etDateTime.setOnClickListener((v) -> pickExpiration());

        // To disable classic input mode
        binding.etDateTime.setOnFocusChangeListener((view, b) -> {
            if (!b) return;

            view.callOnClick();
            view.clearFocus();
        });

        getParentFragmentManager().setFragmentResultListener(ChooseFromMapFragment.REQUEST_POSITION_FROM_MAP, this, (reqKey, bundle) -> {
            setSelectedPosition(bundle.getParcelable(ChooseFromMapFragment.REQUEST_BUNDLE_POSITION_KEY));
        });

        getParentFragmentManager().setFragmentResultListener(SearchResultsBottomSheetFragment.REQUEST_REVERSE_GEOCODED_ADDRESS, this, (reqKey, bundle) -> {
            Address addr = bundle.getParcelable(SearchResultsBottomSheetFragment.BUNDLE_KEY_REVERSE_GEOCODED_ADDRESS);

            LatLng position = new LatLng(addr.getLatitude(), addr.getLongitude());
            String location = AddressFormatter.format(addr);

            setSelectedPosition(position, location);
        });

        binding.btnChoosePlaceFromMap.setOnClickListener((v) -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_AddMemoFragment_to_ChooseFromMapFragment);
        });

        binding.btnSearchPlace.setOnClickListener((v) -> {
            if (binding.etPlace.getText() != null) {
                String search = binding.etPlace.getText().toString();

                if (search.isEmpty()) {
                    binding.tfPlace.setError("");
                }

                new SearchResultsBottomSheetFragment(search)
                        .show(getParentFragmentManager(), SearchResultsBottomSheetFragment.TAG);
            }

        });

        binding.etPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.btnSearchPlace.setEnabled(charSequence.length() != 0);
            }

        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set the FAB from the activity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();

            mainActivity.setFabOnClickListener((View v) -> {
                String title = Objects.requireNonNull(binding.etTitle.getText()).toString();
                String description = Objects.requireNonNull(binding.etDescription.getText()).toString();

                String location = null;
                try {
                    location = Geocoding.getAddressForPosition(selectedPosition, getContext());
                } catch (Exception ignored) {}

                Double latitude = null, longitude = null;
                if (selectedPosition != null) {
                    latitude = selectedPosition.latitude;
                    longitude = selectedPosition.longitude;
                }

                // Validate input
                String errorMessage = null;
                if (title.isEmpty()) {
                    errorMessage = getString(R.string.no_empty_title);
                } else if (selectedExpiration == 0) {
                    errorMessage = getString(R.string.no_expiration);
                }

                if (errorMessage != null) {
                    Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                AppDatabase.getInstance(mainActivity).memoDao().insertAll(
                        new Memo(
                                title,
                                description,
                                latitude,
                                longitude,
                                location,
                                new Date(selectedExpiration),
                                Memo.STATUS_ACTIVE
                        )
                ).addListener(() -> {
                    NavHostFragment.findNavController(this)
                            .popBackStack();
                }, ContextCompat.getMainExecutor(mainActivity));
            });
        }
    }

    private void setSelectedPosition(LatLng position) {
        String location = null;
        try {
            location = Geocoding.getAddressForPosition(position, getContext());
        } catch (Exception ignored) {}

        setSelectedPosition(position, location);
    }

    private void setSelectedPosition(LatLng position, String location) {
        @SuppressLint("DefaultLocale")
        String coordinates = String.format("%.7f, %.7f", position.latitude, position.longitude);

        if (location == null || location.isEmpty()) {
            location = coordinates;
        }

        selectedPosition = position;
        binding.tfPlace.setHelperText(coordinates);
        binding.etPlace.setText(location);
    }

    private void pickExpiration() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_memo_date_time)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds() + 2678400000L) // Today + 31 days
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedExpiration = selection;

            boolean is24H = DateFormat.is24HourFormat(getContext());

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(is24H ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText(R.string.label_memo_date_time)
                    .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                TimeZone tz = TimeZone.getDefault();
                Date now = new Date();
                long offsetFromUtc = tz.getOffset(now.getTime());

                selectedExpiration += (timePicker.getHour() * 60L + timePicker.getMinute()) * 60000L - offsetFromUtc;

                Date expiration = new Date(selectedExpiration);
                binding.etDateTime.setText(SimpleDateFormat.getInstance().format(expiration));
            });

            timePicker.show(getParentFragmentManager(), "AddMemoTimePicker");
        });

        datePicker.show(getParentFragmentManager(), "AddMemoDatePicker");
    }
}