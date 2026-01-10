package com.example.mealrecmmenderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.activities.consumer.AnalyticsActivity;
import com.example.mealrecmmenderandroid.activities.provider.BrowseRecipesActivity;
import com.example.mealrecmmenderandroid.activities.consumer.CookHistoryActivity;
import com.example.mealrecmmenderandroid.activities.consumer.IngredientSelectionActivity;
import com.example.mealrecmmenderandroid.activities.consumer.ProfileActivity;
import com.example.mealrecmmenderandroid.activities.consumer.RecipeDetailActivity;
import com.example.mealrecmmenderandroid.activities.provider.ProviderDashboardActivity;
import com.example.mealrecmmenderandroid.adapters.RecipeAdapter;
import com.example.mealrecmmenderandroid.databinding.ActivityMainBinding;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> allRecipes;
    private Set<String> selectedIngredients;
    private FirebaseAuth mAuth;

    private static final int REQUEST_INGREDIENT_SELECTION = 100;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "=== MainActivity onCreate START ===");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase
        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        selectedIngredients = new HashSet<>();
        allRecipes = new ArrayList<>();

        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        setupListeners();
        loadRecipes();

        // Test connection
        testFirebaseConnection();
    }

    private void testFirebaseConnection() {
        Log.d(TAG, "=== Testing Firebase Connection ===");

        try {
            DatabaseReference testRef = FirebaseDatabase.getInstance().getReference("test_connection");

            testRef.setValue("Connected at " + System.currentTimeMillis())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Firebase Connection Success!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Firebase Connection Failed", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase Error", e);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Healthy Meal Recommender");
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navigationView.setNavigationItemSelectedListener(this);

        // Load appropriate menu based on user type
        if (sessionManager.isProvider()) {
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.navigation_menu_provider);
        }
    }

    private void setupRecyclerView() {
        // FIXED: Removed Context parameter
        recipeAdapter = new RecipeAdapter(new ArrayList<>(),
                selectedIngredients, recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipeId", recipe.getRecipeId());
            startActivity(intent);
        });
        binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recipesRecyclerView.setAdapter(recipeAdapter);
    }

    private void setupListeners() {
        binding.selectIngredientsButton.setOnClickListener(v -> showIngredientSelection());

        // Add Browse Recipes button listener
        binding.browseRecipesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BrowseRecipesActivity.class);
            startActivity(intent);
        });
    }

    private void loadRecipes() {
        binding.progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "=== Loading Recipes from Firebase ===");

        firebaseHelper.getRecipesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allRecipes.clear();

                Log.d(TAG, "Total recipes in database: " + snapshot.getChildrenCount());

                int successCount = 0;
                int errorCount = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Recipe recipe = data.getValue(Recipe.class);
                        if (recipe != null) {
                            allRecipes.add(recipe);
                            successCount++;
                            Log.d(TAG, "✓ Loaded recipe: " + recipe.getRecipeName());
                        } else {
                            errorCount++;
                            Log.w(TAG, "✗ Recipe is null for key: " + data.getKey());
                        }
                    } catch (Exception e) {
                        errorCount++;
                        Log.e(TAG, "✗ Error parsing recipe: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "=== Load Summary ===");
                Log.d(TAG, "Successfully loaded: " + successCount);
                Log.d(TAG, "Errors: " + errorCount);
                Log.d(TAG, "Total in list: " + allRecipes.size());

                binding.progressBar.setVisibility(View.GONE);
                filterRecipes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showIngredientSelection() {
        Intent intent = new Intent(this, IngredientSelectionActivity.class);
        intent.putStringArrayListExtra("selected_ingredients", new ArrayList<>(selectedIngredients));
        startActivityForResult(intent, REQUEST_INGREDIENT_SELECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INGREDIENT_SELECTION && resultCode == RESULT_OK && data != null) {
            ArrayList<String> ingredients = data.getStringArrayListExtra("selected_ingredients");
            if (ingredients != null) {
                selectedIngredients.clear();
                selectedIngredients.addAll(ingredients);
                updateIngredientChips();
                filterRecipes();
            }
        }
    }

    private void updateIngredientChips() {
        binding.ingredientChipGroup.removeAllViews();
        for (String ingredient : selectedIngredients) {
            Chip chip = new Chip(this);
            chip.setText(ingredient);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedIngredients.remove(ingredient);
                updateIngredientChips();
                filterRecipes();
            });
            binding.ingredientChipGroup.addView(chip);
        }
    }

    private void filterRecipes() {
        Log.d(TAG, "=== Filtering Recipes ===");
        Log.d(TAG, "Selected ingredients: " + selectedIngredients.size());
        Log.d(TAG, "Total recipes: " + allRecipes.size());

        if (selectedIngredients.isEmpty()) {
            binding.noRecipesTextView.setVisibility(View.VISIBLE);
            binding.noRecipesTextView.setText("Select ingredients to see recommendations");
            binding.recipesRecyclerView.setVisibility(View.GONE);
            recipeAdapter.updateRecipes(new ArrayList<>());
            Log.d(TAG, "No ingredients selected");
            return;
        }

        List<Recipe> filtered = new ArrayList<>();

        for (Recipe r : allRecipes) {
            try {
                // Use helper method to get ingredients list - FIXED
                List<String> recipeIngredients = r.getIngredientsList();

                if (recipeIngredients != null && !recipeIngredients.isEmpty()) {
                    boolean match = false;

                    for (String ingredientValue : recipeIngredients) {
                        if (ingredientValue == null || ingredientValue.isEmpty()) continue;

                        for (String selectedIng : selectedIngredients) {
                            if (selectedIng == null || selectedIng.isEmpty()) continue;

                            if (ingredientValue.toLowerCase().contains(selectedIng.toLowerCase()) ||
                                    selectedIng.toLowerCase().contains(ingredientValue.toLowerCase())) {
                                match = true;
                                Log.d(TAG, "Match found: " + r.getRecipeName() +
                                        " - ingredient: " + ingredientValue +
                                        " matches: " + selectedIng);
                                break;
                            }
                        }
                        if (match) break;
                    }

                    if (match) {
                        filtered.add(r);
                    }
                } else {
                    Log.d(TAG, "Recipe has no ingredients: " + r.getRecipeName());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error filtering recipe: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Log.d(TAG, "Filtered recipes count: " + filtered.size());

        if (filtered.isEmpty()) {
            binding.noRecipesTextView.setVisibility(View.VISIBLE);
            binding.noRecipesTextView.setText("No matches found. Try selecting different ingredients.");
            binding.recipesRecyclerView.setVisibility(View.GONE);
        } else {
            binding.noRecipesTextView.setVisibility(View.GONE);
            binding.recipesRecyclerView.setVisibility(View.VISIBLE);
        }

        recipeAdapter.updateRecipes(filtered);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Consumer menu items
        if (id == R.id.nav_home) {
            // Already here
        } else if (id == R.id.nav_cook_history) {
            startActivity(new Intent(this, CookHistoryActivity.class));
        } else if (id == R.id.nav_analytics) {
            startActivity(new Intent(this, AnalyticsActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            logoutUser();

            // Provider menu items
        } else if (id == R.id.nav_provider_dashboard) {
            startActivity(new Intent(this, ProviderDashboardActivity.class));
        } else if (id == R.id.nav_browse_recipes) {
            startActivity(new Intent(this, BrowseRecipesActivity.class));
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Sign out from Firebase Auth
                    if (mAuth != null) {
                        mAuth.signOut();
                    }

                    // Clear session manager
                    sessionManager.logoutUser();

                    // Show toast message
                    Toast.makeText(MainActivity.this,
                            "Logged out successfully",
                            Toast.LENGTH_SHORT).show();

                    // Redirect to login screen and clear back stack
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}