package it.passolimirko.memorandum.rw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;

import it.passolimirko.memorandum.R;
import it.passolimirko.memorandum.room.models.Memo;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {

    private final List<Memo> memos;
    private OnMemoClickListener memoClickListener;
    public MemoAdapter(List<Memo> memos) {
        this.memos = memos;
    }

    public OnMemoClickListener getMemoClickListener() {
        return memoClickListener;
    }

    public void setMemoClickListener(OnMemoClickListener memoClickListener) {
        this.memoClickListener = memoClickListener;
    }

    public void setMemos(List<Memo> newMemos) {
        int oldSize = memos.size();
        memos.clear();

        memos.addAll(newMemos);

        notifyItemRangeRemoved(0, oldSize);
        notifyItemRangeInserted(0, memos.size());
    }

    public void add(Memo memo) {
        memos.add(memo);

        notifyItemInserted(memos.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View memoView = inflater.inflate(R.layout.memo_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(memoView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Memo memo = memos.get(position);

        // Mandatory fields first
        holder.tvTitle.setText(memo.title);
        holder.tvExpiration.setText(DateFormat.getDateTimeInstance().format(memo.date));

        // Set description if exists
        if (memo.content == null || memo.content.isEmpty()) holder.tvDescription.setVisibility(View.GONE);
        else holder.tvDescription.setText(memo.content);

        // Location => string associated to lat and lng
        if (memo.location != null && !memo.location.isEmpty())
            holder.tvLocation.setText(memo.location);
        else {
            // No location name, check if there is lat and lng
            if (memo.latitude != null && memo.longitude != null)
                holder.tvLocation.setText(memo.latitude + " " + memo.longitude);
            else {
                // No location, hide location icon and text view
                holder.tvLocation.setVisibility(View.GONE);
                holder.icLocation.setVisibility(View.GONE);
            }
        }

        // Setup click listener
        holder.itemView.setOnClickListener((View v) -> {
            if (memoClickListener != null)
                memoClickListener.onClick(memos.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return memos.size();
    }

    @Override
    public long getItemId(int position) {
        return memos.get(position).id;
    }

    @FunctionalInterface
    public interface OnMemoClickListener {
        void onClick(Memo memo);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvExpiration;
        private final TextView tvLocation;

        private final ImageView icLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.memo_title);
            tvDescription = itemView.findViewById(R.id.memo_desc);
            tvExpiration = itemView.findViewById(R.id.memo_expire);
            tvLocation = itemView.findViewById(R.id.memo_location);

            icLocation = itemView.findViewById(R.id.memo_poi_icon);
        }
    }

}
