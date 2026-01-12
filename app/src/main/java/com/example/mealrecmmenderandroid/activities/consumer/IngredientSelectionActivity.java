package com.example.mealrecmmenderandroid.activities.consumer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.example.mealrecmmenderandroid.databinding.ActivityIngredientSelectionBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class IngredientSelectionActivity extends AppCompatActivity {

    private ActivityIngredientSelectionBinding binding;
    private Set<String> selectedIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIngredientSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedIngredients = new HashSet<>();

        setupToolbar();
        loadPreviousSelections();
        setupListeners();
        updateSelectedCount();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Your Ingredients");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadPreviousSelections() {
        ArrayList<String> previousSelections = getIntent()
                .getStringArrayListExtra("selected_ingredients");
        if (previousSelections != null) {
            for (String ingredient : previousSelections) {
                selectedIngredients.add(ingredient.toLowerCase());
                addIngredientChip(ingredient);
            }
        }
    }

    private void setupListeners() {
        // Clear button acts as ADD button
        binding.clearButton.setOnClickListener(v -> addIngredient());

        // Enter key on keyboard adds ingredient
        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            addIngredient();
            return true;
        });

        // Done button
        binding.doneButton.setOnClickListener(v -> searchRecipes());
    }

    private void addIngredient() {
        String ingredient = binding.searchEditText.getText().toString().trim();

        if (TextUtils.isEmpty(ingredient)) {
            binding.searchEditText.setError("Enter an ingredient");
            return;
        }

        // Check if already added
        String lowerIngredient = ingredient.toLowerCase();
        if (selectedIngredients.contains(lowerIngredient)) {
            Toast.makeText(this, "Already added!", Toast.LENGTH_SHORT).show();
            binding.searchEditText.setText("");
            return;
        }

        // Add ingredient
        selectedIngredients.add(lowerIngredient);
        addIngredientChip(ingredient);

        // Clear input
        binding.searchEditText.setText("");
        binding.searchEditText.requestFocus();

        updateSelectedCount();
    }

    private void addIngredientChip(String ingredient) {
        Chip chip = new Chip(this);
        chip.setText(ingredient);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);

        chip.setOnCloseIconClickListener(v -> {
            binding.ingredientsRecyclerView.removeView(chip);
            selectedIngredients.remove(ingredient.toLowerCase());
            updateSelectedCount();
        });

        // Use RecyclerView as a container for chips
        binding.ingredientsRecyclerView.addView(chip);
    }

    private void searchRecipes() {
        if (selectedIngredients.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("selected_ingredients",
                new ArrayList<>(selectedIngredients));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void updateSelectedCount() {
        int count = selectedIngredients.size();
        binding.selectedCountTextView.setText(count + " ingredient" + (count != 1 ? "s" : "") + " added");
        binding.doneButton.setEnabled(count > 0);
        binding.doneButton.setText(count > 0 ? "SEARCH RECIPES (" + count + ")" : "ADD INGREDIENTS");
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("selected_ingredients",
                new ArrayList<>(selectedIngredients));
        setResult(selectedIngredients.isEmpty() ? RESULT_CANCELED : RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}