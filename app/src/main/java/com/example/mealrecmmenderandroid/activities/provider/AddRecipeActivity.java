package com.example.mealrecmmenderandroid.activities.provider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.databinding.ActivityAddRecipeBinding;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {

    private ActivityAddRecipeBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private Uri selectedImageUri;
    private List<String> ingredientsList;
    private List<Recipe.IngredientDetail> ingredientDetailsList;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        ingredientsList = new ArrayList<>();
        ingredientDetailsList = new ArrayList<>();

        setupToolbar();
        setupSpinners();
        setupListeners();
        setupImagePicker();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Recipe");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        String[] categories = {
                "Spicy",
                "Dessert",
                "Snacks",
                "Heavy Food",
                "Light Meal",
                "Appetizer",
                "Main Course",
                "Soup",
                "Salad",
                "Beverage",
                "Fast Food"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        binding.categorySpinner.setAdapter(categoryAdapter);

        String[] difficulties = {"Easy", "Medium", "Hard"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, difficulties);
        binding.difficultySpinner.setAdapter(difficultyAdapter);

        String[] cuisines = {
                "American",
                "Italian",
                "Chinese",
                "Indian",
                "Mexican",
                "Mediterranean",
                "Japanese",
                "Thai",
                "French",
                "Bangladeshi",
                "Korean",
                "Vietnamese",
                "Middle Eastern",
                "Greek",
                "Spanish",
                "Turkish",
                "Other"
        };
        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, cuisines);
        binding.cuisineSpinner.setAdapter(cuisineAdapter);
    }

    private void setupListeners() {
        binding.selectImageButton.setOnClickListener(v -> pickImage());
        binding.addIngredientButton.setOnClickListener(v -> addIngredient());
        binding.submitRecipeButton.setOnClickListener(v -> submitRecipe());
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            binding.recipeImageView.setImageURI(selectedImageUri);
                            binding.recipeImageView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void addIngredient() {
        String name = binding.ingredientNameEditText.getText().toString().trim();
        String quantity = binding.ingredientQuantityEditText.getText().toString().trim();
        String unit = binding.ingredientUnitEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.ingredientNameEditText.setError("Required");
            return;
        }

        ingredientsList.add(name);

        if (!TextUtils.isEmpty(quantity) && !TextUtils.isEmpty(unit)) {
            Recipe.IngredientDetail detail = new Recipe.IngredientDetail(name, quantity, unit);
            ingredientDetailsList.add(detail);
        }

        Chip chip = new Chip(this);
        String displayText = name;
        if (!TextUtils.isEmpty(quantity) && !TextUtils.isEmpty(unit)) {
            displayText = quantity + " " + unit + " " + name;
        }
        chip.setText(displayText);
        chip.setCloseIconVisible(true);
        final String finalName = name;
        chip.setOnCloseIconClickListener(v -> {
            binding.ingredientsChipGroup.removeView(chip);
            ingredientsList.remove(finalName);
            // Remove from details list
            for (int i = 0; i < ingredientDetailsList.size(); i++) {
                if (ingredientDetailsList.get(i).getName().equals(finalName)) {
                    ingredientDetailsList.remove(i);
                    break;
                }
            }
        });
        binding.ingredientsChipGroup.addView(chip);

        binding.ingredientNameEditText.setText("");
        binding.ingredientQuantityEditText.setText("");
        binding.ingredientUnitEditText.setText("");
    }

    private void submitRecipe() {
        String recipeName = binding.recipeNameEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String instructions = binding.instructionsEditText.getText().toString().trim();
        String caloriesStr = binding.caloriesEditText.getText().toString().trim();
        String prepTimeStr = binding.prepTimeEditText.getText().toString().trim();
        String cookTimeStr = binding.cookTimeEditText.getText().toString().trim();
        String servingsStr = binding.servingsEditText.getText().toString().trim();
        String healthScoreStr = binding.healthScoreEditText.getText().toString().trim();

        String proteinStr = binding.proteinEditText.getText().toString().trim();
        String carbsStr = binding.carbsEditText.getText().toString().trim();
        String fatStr = binding.fatEditText.getText().toString().trim();
        String fiberStr = binding.fiberEditText.getText().toString().trim();

        if (TextUtils.isEmpty(recipeName)) {
            binding.recipeNameEditText.setError("Required");
            binding.recipeNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            binding.descriptionEditText.setError("Required");
            binding.descriptionEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(instructions)) {
            binding.instructionsEditText.setError("Required");
            binding.instructionsEditText.requestFocus();
            return;
        }

        if (ingredientsList.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.submitRecipeButton.setEnabled(false);

        createRecipe(
                recipeName, description, instructions,
                parseIntSafe(caloriesStr, 0),
                parseIntSafe(prepTimeStr, 0),
                parseIntSafe(cookTimeStr, 0),
                parseIntSafe(servingsStr, 1),
                parseDoubleSafe(healthScoreStr, 0),
                parseDoubleSafe(proteinStr, 0),
                parseDoubleSafe(carbsStr, 0),
                parseDoubleSafe(fatStr, 0),
                parseDoubleSafe(fiberStr, 0)
        );
    }

    private void createRecipe(String recipeName, String description, String instructions,
                              int calories, int prepTime, int cookTime, int servings,
                              double healthScore, double protein, double carbs,
                              double fat, double fiber) {

        String recipeId = firebaseHelper.getRecipesRef().push().getKey();
        if (recipeId == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.submitRecipeButton.setEnabled(true);
            Toast.makeText(this, "Failed to create recipe", Toast.LENGTH_SHORT).show();
            return;
        }

        Recipe recipe = new Recipe();
        recipe.setRecipeId(recipeId);
        recipe.setRecipeName(recipeName);
        recipe.setDescription(description);
        recipe.setInstructions(instructions);
        recipe.setProviderId(sessionManager.getUserId());
        recipe.setProviderName(sessionManager.getUserEmail());
        recipe.setCalories(calories);
        recipe.setPreparationTime(prepTime);
        recipe.setCookingTime(cookTime);
        recipe.setServings(servings);
        recipe.setHealthScore(healthScore);
        recipe.setProtein(protein);
        recipe.setCarbs(carbs);
        recipe.setFat(fat);
        recipe.setFiber(fiber);

        String imageUrl;
        if (selectedImageUri != null) {
            imageUrl = "https://via.placeholder.com/600x400/4CAF50/FFFFFF?text=" +
                    recipeName.replace(" ", "+");
        } else {
            imageUrl = "https://via.placeholder.com/600x400/4CAF50/FFFFFF?text=Recipe+Image";
        }
        recipe.setImageUrl(imageUrl);

        recipe.setCategory(binding.categorySpinner.getSelectedItem().toString());
        recipe.setDifficulty(binding.difficultySpinner.getSelectedItem().toString().toLowerCase());
        recipe.setCuisine(binding.cuisineSpinner.getSelectedItem().toString());

        recipe.setIngredientsFromList(ingredientsList);


        Map<String, Recipe.IngredientDetail> detailsMap = new HashMap<>();
        for (int i = 0; i < ingredientDetailsList.size(); i++) {
            detailsMap.put(String.valueOf(i), ingredientDetailsList.get(i));
        }
        recipe.setIngredientDetails(detailsMap);

        recipe.setApproved(true);
        recipe.setCreatedAt(System.currentTimeMillis());

        firebaseHelper.getRecipeRef(recipeId).setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    updateProviderRecipeCount();

                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddRecipeActivity.this,
                            "Recipe added successfully!",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.submitRecipeButton.setEnabled(true);
                    Toast.makeText(AddRecipeActivity.this,
                            "Failed to create recipe: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProviderRecipeCount() {
        String providerId = sessionManager.getUserId();

        firebaseHelper.getUserRef(providerId)
                .child("totalRecipesProvided")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @androidx.annotation.NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @androidx.annotation.NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue + 1);
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(com.google.firebase.database.DatabaseError error,
                                           boolean committed,
                                           com.google.firebase.database.DataSnapshot snapshot) {
                    }
                });
    }

    private int parseIntSafe(String value, int defaultValue) {
        try {
            return TextUtils.isEmpty(value) ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDoubleSafe(String value, double defaultValue) {
        try {
            return TextUtils.isEmpty(value) ? defaultValue : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}