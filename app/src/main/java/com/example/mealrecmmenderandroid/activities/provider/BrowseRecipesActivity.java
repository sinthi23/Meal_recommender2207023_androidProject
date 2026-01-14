package com.example.mealrecmmenderandroid.activities.provider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mealrecmmenderandroid.activities.consumer.RecipeDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.databinding.ActivityBrowseRecipesBinding;
import com.example.mealrecmmenderandroid.adapters.RecipeAdapter;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class BrowseRecipesActivity extends AppCompatActivity {

    private ActivityBrowseRecipesBinding binding;
    private FirebaseHelper firebaseHelper;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> allRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        allRecipes = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        loadAllRecipes();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Browse All Recipes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recipeAdapter = new RecipeAdapter(allRecipes, recipe -> {
            Intent intent = new Intent(BrowseRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipeId", recipe.getRecipeId());
            startActivity(intent);
        });

        binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recipesRecyclerView.setAdapter(recipeAdapter);
    }

    private void loadAllRecipes() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recipesRecyclerView.setVisibility(View.GONE);
        binding.noRecipesTextView.setVisibility(View.GONE);

        android.util.Log.d("BrowseRecipes", "===== LOADING ALL RECIPES =====");

        firebaseHelper.getRecipesRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allRecipes.clear();

                        android.util.Log.d("BrowseRecipes", "Total recipes in database: " + snapshot.getChildrenCount());

                        if (!snapshot.exists()) {
                            android.util.Log.w("BrowseRecipes", "No recipes node exists in database");
                            showEmptyState();
                            return;
                        }

                        int successCount = 0;
                        int errorCount = 0;

                        for (DataSnapshot data : snapshot.getChildren()) {
                            try {
                                Recipe recipe = data.getValue(Recipe.class);
                                if (recipe != null) {
                                    allRecipes.add(recipe);
                                    successCount++;
                                    android.util.Log.d("BrowseRecipes", "✓ Loaded: " + recipe.getRecipeName());
                                } else {
                                    errorCount++;
                                    android.util.Log.w("BrowseRecipes", "✗ Recipe is null for key: " + data.getKey());
                                }
                            } catch (Exception e) {
                                errorCount++;
                                android.util.Log.e("BrowseRecipes", "✗ Error parsing recipe: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        android.util.Log.d("BrowseRecipes", "===== LOAD SUMMARY =====");
                        android.util.Log.d("BrowseRecipes", "Successfully loaded: " + successCount);
                        android.util.Log.d("BrowseRecipes", "Errors: " + errorCount);
                        android.util.Log.d("BrowseRecipes", "Total in list: " + allRecipes.size());
                        android.util.Log.d("BrowseRecipes", "========================");

                        binding.progressBar.setVisibility(View.GONE);

                        if (allRecipes.isEmpty()) {
                            showEmptyState();
                        } else {
                            showRecipes();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("BrowseRecipes", "Database error: " + error.getMessage());
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(BrowseRecipesActivity.this,
                                "Error loading recipes: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        showEmptyState();
                    }
                });
    }

    private void showEmptyState() {
        binding.noRecipesTextView.setVisibility(View.VISIBLE);
        binding.recipesRecyclerView.setVisibility(View.GONE);
        android.util.Log.i("BrowseRecipes", "Showing empty state");
    }

    private void showRecipes() {
        binding.noRecipesTextView.setVisibility(View.GONE);
        binding.recipesRecyclerView.setVisibility(View.VISIBLE);
        recipeAdapter.notifyDataSetChanged();

        android.util.Log.i("BrowseRecipes", "Displaying " + allRecipes.size() + " recipe(s)");
        Toast.makeText(this, "Loaded " + allRecipes.size() + " recipe(s)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}