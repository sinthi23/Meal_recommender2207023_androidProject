package com.example.mealrecmmenderandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.models.Recipe;

import java.util.List;

public class ProviderRecipeAdapter extends RecyclerView.Adapter<ProviderRecipeAdapter.RecipeViewHolder> {

    private final Context context;
    private List<Recipe> recipes;
    private final OnRecipeActionListener listener;

    public interface OnRecipeActionListener {
        void onEditClick(Recipe recipe);
        void onDeleteClick(Recipe recipe);
        void onViewClick(Recipe recipe);
    }

    public ProviderRecipeAdapter(Context context, List<Recipe> recipes, OnRecipeActionListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_provider_recipe, parent, false);
        return new RecipeViewHolder(view);
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
        CardView cardView;
        ImageView recipeImageView;
        TextView recipeNameTextView;
        TextView statusTextView;
        TextView ratingTextView;
        Button editButton;
        Button deleteButton;
        Button viewButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.recipeCard);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            recipeNameTextView = itemView.findViewById(R.id.recipeNameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            viewButton = itemView.findViewById(R.id.viewButton);
        }

        public void bind(Recipe recipe) {
            recipeNameTextView.setText(recipe.getRecipeName());
            ratingTextView.setText(String.format("Rating: %.1f", recipe.getAverageRating()));

            if (recipe.isApproved()) {
                statusTextView.setText("Approved");
                statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                statusTextView.setText("Pending");
                statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            }

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onEditClick(recipe);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteClick(recipe);
                }
            });

            viewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onViewClick(recipe);
                }
            });
        }
    }
}