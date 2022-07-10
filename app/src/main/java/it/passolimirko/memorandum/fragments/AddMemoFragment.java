package it.passolimirko.memorandum.fragments;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import it.passolimirko.memorandum.MainActivity;
import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentAddMemoBinding;

public class AddMemoFragment extends Fragment {

    FragmentAddMemoBinding binding;

    private long selectedExpiration;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddMemoBinding.inflate(inflater, container, false);

        binding.btnEditDateTime.setOnClickListener((v) -> pickExpiration());

        binding.tfDateTimeEl.setOnClickListener((v) -> pickExpiration());

        // To disable classic input mode
        binding.tfDateTimeEl.setOnFocusChangeListener((view, b) -> {
            if (!b) return;

            view.callOnClick();
            view.clearFocus();
        });

        // Get the FAB from the activity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();

            mainActivity.setFabOnClickListener((View v) -> {
                // TODO: Add fab
            });
        }

        binding.btnChoosePlaceFromMap.setOnClickListener((v) -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_AddMemoFragment_to_ChooseFromMapFragment);
        });

        return binding.getRoot();
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
                binding.tfDateTimeEl.setText(SimpleDateFormat.getInstance().format(expiration));
            });

            timePicker.show(getParentFragmentManager(), "AddMemoTimePicker");
        });

        datePicker.show(getParentFragmentManager(), "AddMemoDatePicker");
    }
}