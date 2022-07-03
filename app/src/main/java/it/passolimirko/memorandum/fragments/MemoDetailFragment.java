package it.passolimirko.memorandum.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.text.DateFormat;

import it.passolimirko.memorandum.databinding.FragmentMemoDetailBinding;
import it.passolimirko.memorandum.room.models.Memo;

public class MemoDetailFragment extends Fragment {

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

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}