package com.example.mealrecmmenderandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealrecmmenderandroid.databinding.ItemCookHistoryBinding;
import com.example.mealrecmmenderandroid.models.CookHistory;
import com.example.mealrecmmenderandroid.utils.DateHelper;
import com.example.mealrecmmenderandroid.utils.ImageHelper;

import java.util.List;

public class CookHistoryAdapter extends RecyclerView.Adapter<CookHistoryAdapter.CookHistoryViewHolder> {

    private Context context;
    private List<CookHistory> historyList;
    private OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onHistoryClick(CookHistory history);
        void onViewRecipeClick(CookHistory history);
    }

    public CookHistoryAdapter(Context context, List<CookHistory> historyList,
                              OnHistoryClickListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CookHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCookHistoryBinding binding = ItemCookHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CookHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CookHistoryViewHolder holder, int position) {
        CookHistory history = historyList.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateHistory(List<CookHistory> newHistory) {
        this.historyList = newHistory;
        notifyDataSetChanged();
    }

    class CookHistoryViewHolder extends RecyclerView.ViewHolder {
        private ItemCookHistoryBinding binding;

        public CookHistoryViewHolder(ItemCookHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CookHistory history) {
            binding.recipeNameTextView.setText(history.getRecipeName());
            binding.caloriesTextView.setText(history.getCalories() + " cal");
            binding.cookedDateTextView.setText(DateHelper.formatDate(history.getCookedDate()));
            binding.ratingBar.setRating(history.getUserRating());

            // Notes - check if exists
            if (history.getNotes() != null && !history.getNotes().isEmpty()) {
                binding.notesTextView.setText(history.getNotes());
            } else {
                binding.notesTextView.setText("No notes");
            }

            // Load image
            ImageHelper.loadImage(context, history.getRecipeId(),
                    binding.recipeImageView);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHistoryClick(history);
                }
            });

            binding.viewRecipeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewRecipeClick(history);
                }
            });
        }
    }
}