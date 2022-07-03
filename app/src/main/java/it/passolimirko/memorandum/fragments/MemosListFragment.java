package it.passolimirko.memorandum.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import it.passolimirko.memorandum.MainActivity;
import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentMemosListBinding;
import it.passolimirko.memorandum.room.AppDatabase;
import it.passolimirko.memorandum.room.models.Memo;
import it.passolimirko.memorandum.rw.MemoAdapter;

public class MemosListFragment extends Fragment implements MenuProvider {

    private FragmentMemosListBinding binding;
    private MemoAdapter adapter;

    @SuppressLint("SimpleDateFormat") // TODO: Test only
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMemosListBinding.inflate(inflater, container, false);

        // Setup the common things
        commonSetup();

        // Check if the fragment was created with arguments
        if (getArguments() != null) {
            setupWithArgs(getArguments());
        } else {
            setupDefault();
        }

        return binding.getRoot();
    }

    public void commonSetup() {
        // Setup recycler view
        adapter = new MemoAdapter(new ArrayList<>());
        binding.activeMemoRw.setAdapter(adapter);
        binding.activeMemoRw.setLayoutManager(new LinearLayoutManager(getContext()));

        // When the user click on a memo
        adapter.setMemoClickListener((Memo memo) -> {
            Bundle data = new Bundle();
            data.putParcelable(MemoDetailFragment.BUNDLE_MEMO_KEY, memo);

            NavHostFragment.findNavController(MemosListFragment.this)
                    .navigate(R.id.action_ActiveMemosFragment_to_MemoDetailFragment, data);
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

        // Tell the activity that and options menu is available
        FragmentActivity activity = getActivity();
        if (activity != null) activity.addMenuProvider(this);
    }

    private void setupDefault() {
        // Show active memos
        // The toolbar title is setted by the navigation graph
        
        // Listen to room live data
        AppDatabase.getInstance(requireContext()).memoDao().getActive().observe(getViewLifecycleOwner(),
                memos -> {
                    adapter.setMemos(memos);
                    updateNoMemosMessageVisibility();
                });
    }

    private void setupWithArgs(@NonNull Bundle args) {
        switch (args.getString("mode")) {
            case "memos-list":
                // The list of memos is passed within the bundle
                // TODO: Get the list
                break;

            case "completed": // TODO: implement
            case "expired": // TODO: implement

            default:
                // Fallback to setupDefault
                setupDefault();
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle("your title");
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
            NavHostFragment.findNavController(MemosListFragment.this)
                    .navigate(R.id.action_ActiveMemosFragment_to_MemosMapFragment);
            return true;
        }

        return false;
    }
}