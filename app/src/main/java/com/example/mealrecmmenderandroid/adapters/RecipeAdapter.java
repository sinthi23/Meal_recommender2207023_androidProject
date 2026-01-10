package com.example.mealrecmmenderandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.databinding.ItemRecipeBinding;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.utils.ImageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

            // Calculate total time - FIXED
            int totalTime = recipe.getPreparationTime() + recipe.getCookingTime();
            binding.timeTextView.setText(totalTime + " min");

            binding.healthScoreTextView.setText("Health: " +
                    String.format("%.0f%%", recipe.getHealthScore()));
            binding.ratingBar.setRating((float) recipe.getAverageRating());

            // Load image
            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                ImageHelper.loadImage(context, recipe.getImageUrl(), binding.recipeImageView);
            }

            // Calculate missing ingredients - FIXED
            List<String> recipeIngredients = getRecipeIngredientsList(recipe);
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

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }

        // Helper method to get ingredients list from Recipe
        private List<String> getRecipeIngredientsList(Recipe recipe) {
            List<String> ingredientsList = new ArrayList<>();

            // Try to get from ingredients map
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                ingredientsList.addAll(recipe.getIngredients().values());
            }

            // If empty, try ingredient details
            if (ingredientsList.isEmpty() && recipe.getIngredientDetails() != null) {
                for (Recipe.IngredientDetail detail : recipe.getIngredientDetails().values()) {
                    if (detail.getName() != null) {
                        ingredientsList.add(detail.getName());
                    }
                }
            }

            return ingredientsList;
        }

        private int calculateMissingIngredients(List<String> recipeIngredients) {
            if (recipeIngredients == null || recipeIngredients.isEmpty()) {
                return 0;
            }

            if (selectedIngredients == null || selectedIngredients.isEmpty()) {
                return recipeIngredients.size();
            }

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
            if (difficulty == null) {
                binding.difficultyBadge.setText("Medium");
                binding.difficultyBadge.setBackgroundColor(
                        context.getResources().getColor(R.color.difficulty_medium));
                return;
            }

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
                default:
                    binding.difficultyBadge.setText(difficulty);
                    binding.difficultyBadge.setBackgroundColor(
                            context.getResources().getColor(R.color.difficulty_medium));
                    break;
            }
        }
    }
}