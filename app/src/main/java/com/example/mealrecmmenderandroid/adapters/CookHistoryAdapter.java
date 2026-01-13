package com.example.mealrecmmenderandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealrecmmenderandroid.databinding.ItemCookHistoryBinding;
import com.example.mealrecmmenderandroid.models.CookHistory;
import com.example.mealrecmmenderandroid.utils.ImageHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            // Recipe name
            binding.recipeNameTextView.setText(history.getRecipeName() != null ?
                    history.getRecipeName() : "Unknown Recipe");

            // Calories
            binding.caloriesTextView.setText(history.getCalories() + " cal");

            // Date - Format timestamp
            binding.cookedDateTextView.setText(formatDate(history.getTimestamp()));

            // Rating
            binding.ratingBar.setRating(history.getRating());

            // Notes - check if exists
            if (history.getNotes() != null && !history.getNotes().isEmpty()) {
                binding.notesTextView.setText(history.getNotes());
                binding.notesTextView.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.notesTextView.setVisibility(android.view.View.GONE);
            }

            // Load recipe image if available - FIXED
            if (history.getRecipeId() != null && !history.getRecipeId().isEmpty()) {
                loadRecipeImageFromFirebase(history.getRecipeId());
            } else {
                binding.recipeImageView.setImageResource(
                        com.example.mealrecmmenderandroid.R.drawable.ic_image_placeholder);
            }

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

        private void loadRecipeImageFromFirebase(String recipeId) {
            com.example.mealrecmmenderandroid.helpers.FirebaseHelper.getInstance()
                    .getRecipeRef(recipeId)
                    .child("imageUrl")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            String imageUrl = snapshot.getValue(String.class);
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                ImageHelper.loadImage(context, imageUrl, binding.recipeImageView);
                            } else {
                                binding.recipeImageView.setImageResource(
                                        com.example.mealrecmmenderandroid.R.drawable.ic_image_placeholder);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                            binding.recipeImageView.setImageResource(
                                    com.example.mealrecmmenderandroid.R.drawable.ic_image_placeholder);
                        }
                    });
        }

        private String formatDate(long timestamp) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            } catch (Exception e) {
                return "Unknown date";
            }
        }
    }
}