package it.passolimirko.memorandum.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import it.passolimirko.memorandum.MainActivity;
import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentActiveMemosBinding;
import it.passolimirko.memorandum.room.AppDatabase;
import it.passolimirko.memorandum.room.models.Memo;
import it.passolimirko.memorandum.rw.MemoAdapter;

public class ActiveMemosFragment extends Fragment implements MenuProvider {

    private FragmentActiveMemosBinding binding;
    private MemoAdapter adapter;

    @SuppressLint("SimpleDateFormat") // TODO: Test only
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentActiveMemosBinding.inflate(inflater, container, false);

        // Setup recycler view
        adapter = new MemoAdapter(new ArrayList<>());
        binding.activeMemoRw.setAdapter(adapter);
        binding.activeMemoRw.setLayoutManager(new LinearLayoutManager(getContext()));

        // When the user click on a memo
        adapter.setMemoClickListener((Memo memo) -> {
            Bundle data = new Bundle();
            data.putParcelable(MemoDetailFragment.BUNDLE_MEMO_KEY, memo);

            NavHostFragment.findNavController(ActiveMemosFragment.this)
                    .navigate(R.id.action_ActiveMemosFragment_to_MemoDetailFragment, data);
        });

        // Listen to room live data
        AppDatabase.getInstance(requireContext()).memoDao().getActive().observe(getViewLifecycleOwner(),
                memos -> {
                    adapter.setMemos(memos);
                    updateNoMemosMessageVisibility();
                });

        // Get the FAB from the activity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();

            mainActivity.setFabOnClickListener((View v) -> new Thread(() -> {
                // TODO: Test only
                try {
                    AppDatabase.getInstance(getContext()).memoDao().insertAll(
                            new Memo("Fare lavatrice", "Capi bianchi e colorati", 45, 9, new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2022-07-12 10:30"), Memo.STATUS_ACTIVE),
                            new Memo("Cucinare", "Pasta aglio oglio e peperoncino", 45, 9, new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2022-07-01 12:30"), Memo.STATUS_ACTIVE),
                            new Memo("Veterinario", "Portare black dal veterinario", 45, 10, new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2022-07-20 15:15"), Memo.STATUS_ACTIVE)
                    );
                } catch (Exception ignored) {
                }
            }).start());
        }

        FragmentActivity activity = getActivity();
        if (activity != null) activity.addMenuProvider(this);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        FragmentActivity activity = getActivity();
        if (activity != null) activity.removeMenuProvider(this);
    }

    private void updateNoMemosMessageVisibility() {
        if (binding.activeMemoRw.getAdapter() == null || binding.activeMemoRw.getAdapter().getItemCount() == 0) {
            binding.tvNoActiveMemo.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoActiveMemo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_todos, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_showMemosInMap) {
            NavHostFragment.findNavController(ActiveMemosFragment.this)
                    .navigate(R.id.action_ActiveMemosFragment_to_MemosMapFragment);
            return true;
        }

        return false;
    }
}