package com.example.mealrecmmenderandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.models.Ingredient;

import java.util.List;
import java.util.Set;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private Context context;
    private List<Ingredient> ingredients;
    private Set<String> selectedIngredients;
    private OnIngredientSelectedListener listener;

    public interface OnIngredientSelectedListener {
        void onIngredientSelected(Ingredient ingredient, boolean isSelected);
    }

    public IngredientAdapter(Context context, List<Ingredient> ingredients,
                             Set<String> selectedIngredients,
                             OnIngredientSelectedListener listener) {
        this.context = context;
        this.ingredients = ingredients;
        this.selectedIngredients = selectedIngredients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public void updateIngredients(List<Ingredient> newIngredients) {
        this.ingredients = newIngredients;
        notifyDataSetChanged();
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameTextView;
        TextView categoryTextView;
        CheckBox checkBox;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.ingredientCard);
            nameTextView = itemView.findViewById(R.id.ingredientNameTextView);
            categoryTextView = itemView.findViewById(R.id.ingredientCategoryTextView);
            checkBox = itemView.findViewById(R.id.ingredientCheckBox);
        }

        public void bind(Ingredient ingredient) {
            nameTextView.setText(ingredient.getName());
            categoryTextView.setText(ingredient.getCategory());

            boolean isSelected = selectedIngredients.contains(ingredient.getName());
            checkBox.setChecked(isSelected);

            cardView.setOnClickListener(v -> {
                boolean newState = !checkBox.isChecked();
                checkBox.setChecked(newState);

                if (newState) {
                    selectedIngredients.add(ingredient.getName());
                } else {
                    selectedIngredients.remove(ingredient.getName());
                }

                if (listener != null) {
                    listener.onIngredientSelected(ingredient, newState);
                }
            });

            checkBox.setOnClickListener(v -> {
                boolean isChecked = checkBox.isChecked();

                if (isChecked) {
                    selectedIngredients.add(ingredient.getName());
                } else {
                    selectedIngredients.remove(ingredient.getName());
                }

                if (listener != null) {
                    listener.onIngredientSelected(ingredient, isChecked);
                }
            });
        }
    }
}