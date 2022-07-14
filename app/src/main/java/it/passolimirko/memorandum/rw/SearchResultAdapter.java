package it.passolimirko.memorandum.rw;

import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.utils.AddressFormatter;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final List<Address> items;

    private OnItemClickListener itemClickListener;

    public SearchResultAdapter(List<Address> items) {
        this.items = items;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setResults(List<Address> newResults) {
        int oldSize = items.size();
        items.clear();

        items.addAll(newResults);

        notifyItemRangeRemoved(0, oldSize);
        notifyItemRangeInserted(0, items.size());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.v("ADAPTER", "onCreateViewHolder");

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View memoView = inflater.inflate(R.layout.result_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(memoView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvText.setText(AddressFormatter.format(items.get(0)));

        holder.itemView.setOnClickListener((v) -> {
            if (itemClickListener != null)
                itemClickListener.onClick(items.get(0));
        });
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onClick(Address item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvText = itemView.findViewById(R.id.result_text);
        }
    }

}
