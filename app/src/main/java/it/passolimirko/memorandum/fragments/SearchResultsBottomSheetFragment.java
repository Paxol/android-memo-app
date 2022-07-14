package it.passolimirko.memorandum.fragments;

import android.app.Dialog;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentSearchResultsListBinding;
import it.passolimirko.memorandum.rw.SearchResultAdapter;
import it.passolimirko.memorandum.utils.Geocoding;

public class SearchResultsBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String TAG = "SearchResultsBottomSheetFragment";

    public static final String REQUEST_REVERSE_GEOCODED_ADDRESS = "REQUEST_REVERSE_GEOCODED_ADDRESS";
    public static final String BUNDLE_KEY_REVERSE_GEOCODED_ADDRESS = "REVERSE_GEOCODED_ADDRESS";

    FragmentSearchResultsListBinding binding;

    String searchTerm;

    public SearchResultsBottomSheetFragment(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = (FrameLayout) d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) {
                return;
            }

            BottomSheetBehavior.from(bottomSheet).setDraggable(false);
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultsListBinding.inflate(inflater, container, false);

        new Thread(() -> {
            List<Address> results = null;
            try {
                results = Geocoding.searchAddressesForLocation(searchTerm, getContext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                final List<Address> finalRes = results;
                Handler handler = new Handler(Looper.getMainLooper());

                handler.post(() -> {
                    binding.pbLoading.setVisibility(View.GONE);

                    if (finalRes == null) {
                        binding.tvNoResults.setText(R.string.error_occurred_internet_connection);
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (finalRes.size() == 0) {
                        binding.tvNoResults.setText(R.string.no_results);
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                    } else {
                        SearchResultAdapter adapter = new SearchResultAdapter(finalRes);
                        adapter.setItemClickListener((address) -> {
                            Bundle result = new Bundle();
                            result.putParcelable(BUNDLE_KEY_REVERSE_GEOCODED_ADDRESS, address);

                            getParentFragmentManager().setFragmentResult(REQUEST_REVERSE_GEOCODED_ADDRESS, result);

                            dismiss();
                        });

                        binding.rwResults.setLayoutManager(new LinearLayoutManager(getContext()));
                        binding.rwResults.setAdapter(adapter);
                    }
                });
            }
        }).start();

        return binding.getRoot();
    }
}
