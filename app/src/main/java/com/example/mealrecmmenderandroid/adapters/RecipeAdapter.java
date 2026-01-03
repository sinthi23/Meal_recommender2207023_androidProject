package com.example.mealrecmmenderandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.databinding.ItemRecipeBinding;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.utils.ImageHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final Context context;
    private List<Recipe> recipes;
    private final Set<String> selectedIngredients;
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, List<Recipe> recipes,
                         Set<String> selectedIngredients,
                         OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.selectedIngredients = selectedIngredients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RecipeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipeBinding binding;

        public RecipeViewHolder(ItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Recipe recipe) {
            binding.recipeNameTextView.setText(recipe.getRecipeName());
            binding.caloriesTextView.setText(recipe.getCalories() + " cal");
            binding.timeTextView.setText(recipe.getTotalTime() + " min");
            binding.healthScoreTextView.setText("Health: " +
                    String.format("%.0f%%", recipe.getHealthScore()));
            binding.ratingBar.setRating((float) recipe.getAverageRating());

            // Load image
            ImageHelper.loadImage(context, recipe.getImageUrl(), binding.recipeImageView);

            // Calculate missing ingredients
            List<String> recipeIngredients = recipe.getIngredientsList();
            int missingCount = calculateMissingIngredients(recipeIngredients);

            if (missingCount == 0) {
                binding.missingIngredientsTextView.setText("All ingredients available!");
                binding.missingIngredientsTextView.setTextColor(
                        context.getResources().getColor(R.color.success_color));
            } else {
                binding.missingIngredientsTextView.setText("Missing: " + missingCount + " items");
                binding.missingIngredientsTextView.setTextColor(
                        context.getResources().getColor(R.color.warning_color));
            }

            // Difficulty badge
            setDifficultyBadge(recipe.getDifficulty());

            itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
        }

        private int calculateMissingIngredients(List<String> recipeIngredients) {
            int missingCount = 0;
            for (String ingredient : recipeIngredients) {
                boolean found = false;
                for (String selected : selectedIngredients) {
                    if (ingredient.toLowerCase().contains(selected.toLowerCase()) ||
                            selected.toLowerCase().contains(ingredient.toLowerCase())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    missingCount++;
                }
            }
            return missingCount;
        }

        private void setDifficultyBadge(String difficulty) {
            if (difficulty == null) return;

            switch (difficulty.toLowerCase()) {
                case "easy":
                    binding.difficultyBadge.setText("Easy");
                    binding.difficultyBadge.setBackgroundColor(
                            context.getResources().getColor(R.color.difficulty_easy));
                    break;
                case "medium":
                    binding.difficultyBadge.setText("Medium");
                    binding.difficultyBadge.setBackgroundColor(
                            context.getResources().getColor(R.color.difficulty_medium));
                    break;
                case "hard":
                    binding.difficultyBadge.setText("Hard");
                    binding.difficultyBadge.setBackgroundColor(
                            context.getResources().getColor(R.color.difficulty_hard));
                    break;
            }
        }
    }
}