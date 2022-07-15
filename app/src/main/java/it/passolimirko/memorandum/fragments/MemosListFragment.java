package it.passolimirko.memorandum.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.passolimirko.memorandum.MainActivity;
import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.databinding.FragmentMemosListBinding;
import it.passolimirko.memorandum.room.AppDatabase;
import it.passolimirko.memorandum.room.models.Memo;
import it.passolimirko.memorandum.rw.MemoAdapter;
import it.passolimirko.memorandum.utils.MemoUtils;

public class MemosListFragment extends Fragment implements MenuProvider {

    public static final int MODE_LIST = 0;
    public static final int MODE_ACTIVE = 1;
    public static final int MODE_COMPLETED = 2;
    public static final int MODE_EXPIRED = 3;

    private int selectedMode = -1;

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
                    .navigate(R.id.action_MemosListFragment_to_MemoDetailFragment, data);
        });

        // Get the FAB from the activity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();

            mainActivity.setFabOnClickListener((View v) ->
                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main)
                            .navigate(R.id.To_AddMemoFragment)
            );
        }

        // Tell the activity that and options menu is available
        addToolbarMenu();
    }

    private void setupDefault() {
        // Show active memos
        selectedMode = MODE_ACTIVE;

        // The toolbar title is setted by the navigation graph

        // Listen to room live data
        AppDatabase.getInstance(requireContext()).memoDao().getActive().observe(getViewLifecycleOwner(),
                this::setMemosList);
    }

    private void setupWithArgs(@NonNull Bundle args) {
        String title = getResources().getString(R.string.memos_list_fallback_title);

        selectedMode = args.getInt("mode");

        // Gat activity to access toolbar
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        switch (selectedMode) {
            case MODE_LIST:
                // The list of memos is passed within the bundle
                // Convert the array
                Parcelable[] parcelableArray = args.getParcelableArray("memos");
                Memo[] resultArray = null;
                if (parcelableArray != null) {
                    resultArray = Arrays.copyOf(parcelableArray, parcelableArray.length, Memo[].class);
                }

                // Set the adapter's list
                setMemosList(Arrays.asList(resultArray != null ? resultArray : new Memo[0]));

                // Remove menu
                removeToolbarMenu();

                // Show back arrow in toolbar
                if (activity != null && activity.getSupportActionBar() != null)
                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                break;

            case MODE_COMPLETED: // TODO: implement
            case MODE_EXPIRED: // TODO: implement

            case MODE_ACTIVE:
            default:
                // Fallback to setupDefault
                setupDefault();
                return;
        }

        // Change toolbar title
        if (activity != null && activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        removeToolbarMenu();
    }

    private void addToolbarMenu() {
        FragmentActivity activity = getActivity();
        if (activity != null) activity.addMenuProvider(this);
    }

    private void removeToolbarMenu() {
        FragmentActivity activity = getActivity();
        if (activity != null) activity.removeMenuProvider(this);
    }

    private void setMemosList(List<Memo> memos) {
        try {
            boolean updated = MemoUtils.checkLocation(memos, getContext());

            if (updated)
                for (Memo m : memos) {
                    AppDatabase.getInstance(getContext()).memoDao().updateLocation(m.id, m.location);
                }
        } catch (IOException e) {
            e.printStackTrace();
        }

        adapter.setMemos(memos);
        updateNoMemosMessageVisibility();
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

        switch (selectedMode) {
            case MODE_LIST:
                menu.removeGroup(R.id.group_memosStatuses);
                break;
            case MODE_ACTIVE:
                menu.removeItem(R.id.action_showActiveMemos);
                break;
            case MODE_COMPLETED:
                menu.removeItem(R.id.action_showCompletedMemos);
                break;
            case MODE_EXPIRED:
                menu.removeItem(R.id.action_showExpiredMemos);
                break;
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_showMemosInMap) {
            NavHostFragment.findNavController(MemosListFragment.this)
                    .navigate(R.id.action_MemosListFragment_to_MemosMapFragment);
            return true;
        }

        return false;
    }
}