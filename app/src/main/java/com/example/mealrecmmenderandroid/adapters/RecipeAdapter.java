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

    private List<Recipe> recipes;
    private Set<String> selectedIngredients;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    // Constructor for Browse Recipes (no selected ingredients)
    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.selectedIngredients = null;
        this.listener = listener;
    }

    // Constructor for Recipe List with ingredient filtering
    public RecipeAdapter(List<Recipe> recipes, Set<String> selectedIngredients, OnRecipeClickListener listener) {
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
        return recipes != null ? recipes.size() : 0;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipeBinding binding;
        private final Context context;

        public RecipeViewHolder(ItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = binding.getRoot().getContext();
        }

        public void bind(Recipe recipe) {
            if (recipe == null) return;

            // Set basic info with null checks
            binding.recipeNameTextView.setText(recipe.getRecipeName() != null ? recipe.getRecipeName() : "Unknown Recipe");
            binding.caloriesTextView.setText(recipe.getCalories() + " cal");

            // Calculate total time
            int totalTime = recipe.getPreparationTime() + recipe.getCookingTime();
            binding.timeTextView.setText(totalTime + " min");

            // Health score
            binding.healthScoreTextView.setText("Health: " +
                    String.format("%.0f%%", recipe.getHealthScore()));

            // Rating
            binding.ratingBar.setRating((float) recipe.getAverageRating());

            // Load image
            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                ImageHelper.loadImage(context, recipe.getImageUrl(), binding.recipeImageView);
            } else {
                binding.recipeImageView.setImageResource(R.drawable.ic_image_placeholder);
            }

            // Calculate missing ingredients
            List<String> recipeIngredients = getRecipeIngredientsList(recipe);

            if (selectedIngredients != null && !selectedIngredients.isEmpty()) {
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
            } else {
                // No selected ingredients - show total count
                binding.missingIngredientsTextView.setText(recipeIngredients.size() + " ingredients");
                binding.missingIngredientsTextView.setTextColor(
                        context.getResources().getColor(R.color.text_secondary));
            }

            // Difficulty badge
            setDifficultyBadge(recipe.getDifficulty());

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }

        // Helper method to get ingredients list from Recipe - FIXED
        private List<String> getRecipeIngredientsList(Recipe recipe) {
            List<String> ingredientsList = new ArrayList<>();

            try {
                // Try to get from ingredients list
                List<String> recipeIngredients = recipe.getIngredientsList();
                if (recipeIngredients != null && !recipeIngredients.isEmpty()) {
                    ingredientsList.addAll(recipeIngredients);
                }

                // If empty, try from ingredients map
                if (ingredientsList.isEmpty()) {
                    Map<String, String> ingredientsMap = recipe.getIngredientsMap();
                    if (ingredientsMap != null && !ingredientsMap.isEmpty()) {
                        ingredientsList.addAll(ingredientsMap.values());
                    }
                }

                // If still empty, try ingredient details
                if (ingredientsList.isEmpty()) {
                    Map<String, Recipe.IngredientDetail> detailsMap = recipe.getIngredientDetailsMap();
                    if (detailsMap != null && !detailsMap.isEmpty()) {
                        for (Recipe.IngredientDetail detail : detailsMap.values()) {
                            if (detail != null && detail.getName() != null) {
                                ingredientsList.add(detail.getName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("RecipeAdapter", "Error getting ingredients: " + e.getMessage());
                e.printStackTrace();
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
                if (ingredient == null || ingredient.isEmpty()) continue;

                boolean found = false;
                for (String selected : selectedIngredients) {
                    if (selected == null || selected.isEmpty()) continue;

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
            if (difficulty == null || difficulty.isEmpty()) {
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