package it.passolimirko.memorandum.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
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
    private LiveData<List<Memo>> currentObserver = null;

    private FragmentMemosListBinding binding;
    private MemoAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new MemoAdapter(new ArrayList<>());

        // When the user click on a memo
        adapter.setMemoClickListener((Memo memo) -> {
            Bundle data = new Bundle();
            data.putParcelable(MemoDetailFragment.BUNDLE_MEMO_KEY, memo);

            NavHostFragment.findNavController(MemosListFragment.this)
                    .navigate(R.id.action_MemosListFragment_to_MemoDetailFragment, data);
        });
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMemosListBinding.inflate(inflater, container, false);

        if (selectedMode == -1) {
            // Setup never ran
            // Check if the fragment was created with arguments
            if (getArguments() != null) {
                setupWithArgs(getArguments());
            } else {
                selectedMode = MODE_ACTIVE;
                setup(null);
            }
        } else {
            // Setup ran, the adapter is ok, but we need to update the toolbar title
            String title;

            switch (selectedMode) {
                case MODE_LIST:
                    title = getString(R.string.memos_list_fallback_title);
                    break;
                case MODE_COMPLETED:
                    title = getString(R.string.completed_memos);
                    break;

                case MODE_EXPIRED:
                    title = getString(R.string.expired_memos);
                    break;

                case MODE_ACTIVE:
                default:
                    title = getString(R.string.active_memos_fragment_label);
                    break;
            }

            // Change toolbar title
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null && activity.getSupportActionBar() != null)
                activity.getSupportActionBar().setTitle(title);
        }

        // Setup recycler view
        binding.activeMemoRw.setAdapter(adapter);
        binding.activeMemoRw.setLayoutManager(new LinearLayoutManager(getContext()));

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

        return binding.getRoot();
    }

    private void setLiveDataObserver(LiveData<List<Memo>> newObserver) {
        if (currentObserver != null) {
            currentObserver.removeObservers(getViewLifecycleOwner());
        }

        currentObserver = newObserver;

        // Listen to room live data
        newObserver.observe(getViewLifecycleOwner(), this::setMemosList);
    }

    private void setupWithArgs(@NonNull Bundle args) {
        selectedMode = args.getInt("mode");

        setup(args);
    }

    private void setup(@Nullable Bundle args) {
        // Gat activity to access toolbar
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        String title = getResources().getString(R.string.memos_list_fallback_title);

        switch (selectedMode) {
            case MODE_LIST:
                // The list of memos is passed within the bundle
                if (args == null)
                    throw new IllegalArgumentException("Bundle args cannot be null in list mode");

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

            case MODE_COMPLETED:
                setLiveDataObserver(AppDatabase.getInstance(getContext()).memoDao().getCompleted());
                title = getString(R.string.completed_memos);
                break;

            case MODE_EXPIRED:
                setLiveDataObserver(AppDatabase.getInstance(getContext()).memoDao().getExpired());
                title = getString(R.string.expired_memos);
                break;

            case MODE_ACTIVE:
            default:
                // Set to the correct value in case of default
                selectedMode = MODE_ACTIVE;

                setLiveDataObserver(AppDatabase.getInstance(getContext()).memoDao().getActive());
                title = getString(R.string.active_memos_fragment_label);
                break;
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
            switch (selectedMode) {
                case MODE_LIST:
                    binding.tvNoActiveMemo.setText(R.string.no_memo);
                    break;
                case MODE_ACTIVE:
                    binding.tvNoActiveMemo.setText(R.string.no_active_memo);
                    break;
                case MODE_COMPLETED:
                    binding.tvNoActiveMemo.setText(R.string.no_completed_memo);
                    break;
                case MODE_EXPIRED:
                    binding.tvNoActiveMemo.setText(R.string.no_expired_memo);
                    break;
            }

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

        if (selectedMode != MODE_ACTIVE) {
            // Show map option only for active memos
            menu.removeItem(R.id.action_showMemosInMap);
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_showMemosInMap) {
            NavHostFragment.findNavController(MemosListFragment.this)
                    .navigate(R.id.action_MemosListFragment_to_MemosMapFragment);
            return true;
        }

        boolean invaliateMenu = false;

        if (menuItem.getItemId() == R.id.action_showActiveMemos) {
            // Show active memos
            selectedMode = MODE_ACTIVE;
            setup(null);
            invaliateMenu = true;
        } else if (menuItem.getItemId() == R.id.action_showCompletedMemos) {
            // Show completed memos
            selectedMode = MODE_COMPLETED;
            setup(null);
            invaliateMenu = true;
        } else if (menuItem.getItemId() == R.id.action_showExpiredMemos) {
            // Show expired memos
            selectedMode = MODE_EXPIRED;
            setup(null);
            invaliateMenu = true;
        }

        if (invaliateMenu) {
            if (getActivity() != null) getActivity().invalidateMenu();

            return true;
        }

        return false;
    }
}