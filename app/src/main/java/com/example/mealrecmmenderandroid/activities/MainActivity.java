package com.example.mealrecmmenderandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.activities.consumer.AnalyticsActivity;
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

    private static final int REQUEST_INGREDIENT_SELECTION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        selectedIngredients = new HashSet<>();
        allRecipes = new ArrayList<>();

        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        setupListeners();
        loadRecipes();
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

        // Update navigation menu based on user type
        if (sessionManager.isProvider()) {
            binding.navigationView.getMenu().clear();
            binding.navigationView.inflateMenu(R.menu.navigation_menu_provider);
        }
    }

    private void setupRecyclerView() {
        recipeAdapter = new RecipeAdapter(this, new ArrayList<>(),
                selectedIngredients, new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                MainActivity.this.onRecipeClick(recipe);
            }
        });
        binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recipesRecyclerView.setAdapter(recipeAdapter);
    }

    private void setupListeners() {
        binding.selectIngredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIngredientSelection();
            }
        });
    }

    private void loadRecipes() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.getRecipesRef()
                .orderByChild("isApproved")
                .equalTo(true)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allRecipes.clear();
                        for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                            Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                            if (recipe != null) {
                                allRecipes.add(recipe);
                            }
                        }
                        binding.progressBar.setVisibility(View.GONE);
                        filterRecipes();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this,
                                "Error loading recipes: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showIngredientSelection() {
        Intent intent = new Intent(this, IngredientSelectionActivity.class);
        intent.putStringArrayListExtra("selected_ingredients",
                new ArrayList<>(selectedIngredients));
        startActivityForResult(intent, REQUEST_INGREDIENT_SELECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INGREDIENT_SELECTION && resultCode == RESULT_OK && data != null) {
            ArrayList<String> ingredients =
                    data.getStringArrayListExtra("selected_ingredients");
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
            final String finalIngredient = ingredient;
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedIngredients.remove(finalIngredient);
                    binding.ingredientChipGroup.removeView(chip);
                    filterRecipes();
                }
            });
            binding.ingredientChipGroup.addView(chip);
        }
    }

    private void filterRecipes() {
        if (selectedIngredients.isEmpty()) {
            binding.noRecipesTextView.setVisibility(View.VISIBLE);
            binding.noRecipesTextView.setText("Select ingredients to see matching recipes");
            binding.recipesRecyclerView.setVisibility(View.GONE);
            recipeAdapter.updateRecipes(new ArrayList<>());
            return;
        }

        List<Recipe> filteredRecipes = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (hasMatchingIngredients(recipe)) {
                filteredRecipes.add(recipe);
            }
        }

        if (filteredRecipes.isEmpty()) {
            binding.noRecipesTextView.setText("No recipes found with selected ingredients");
            binding.noRecipesTextView.setVisibility(View.VISIBLE);
            binding.recipesRecyclerView.setVisibility(View.GONE);
        } else {
            binding.noRecipesTextView.setVisibility(View.GONE);
            binding.recipesRecyclerView.setVisibility(View.VISIBLE);
        }

        recipeAdapter.updateRecipes(filteredRecipes);
    }

    private boolean hasMatchingIngredients(Recipe recipe) {
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return false;
        }

        List<String> recipeIngredients = recipe.getIngredientsList();
        int matchCount = 0;

        for (String recipeIngredient : recipeIngredients) {
            for (String selectedIngredient : selectedIngredients) {
                if (recipeIngredient.toLowerCase().contains(selectedIngredient.toLowerCase()) ||
                        selectedIngredient.toLowerCase().contains(recipeIngredient.toLowerCase())) {
                    matchCount++;
                    break;
                }
            }
        }

        // Recipe matches if at least 50% of ingredients are available
        return matchCount >= (recipeIngredients.size() * 0.5);
    }

    private void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getRecipeId());
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home
        } else if (id == R.id.nav_cook_history) {
            startActivity(new Intent(this, CookHistoryActivity.class));
        } else if (id == R.id.nav_analytics) {
            startActivity(new Intent(this, AnalyticsActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_provider_dashboard) {
            startActivity(new Intent(this, ProviderDashboardActivity.class));
        } else if (id == R.id.nav_logout) {
            logout();
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        firebaseHelper.getAuth().signOut();
        sessionManager.logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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