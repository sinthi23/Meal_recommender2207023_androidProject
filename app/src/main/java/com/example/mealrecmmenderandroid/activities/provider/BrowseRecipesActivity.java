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
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.databinding.ActivityBrowseRecipesBinding;
import com.example.mealrecmmenderandroid.adapters.RecipeAdapter;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BrowseRecipesActivity extends AppCompatActivity {

    private ActivityBrowseRecipesBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> allRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
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
        recipeAdapter = new RecipeAdapter(
                this,
                new ArrayList<>(),
                new HashSet<>(),
                recipe -> {
                    Intent intent = new Intent(this, RecipeDetailActivity.class);
                    intent.putExtra("recipe_id", recipe.getRecipeId());
                    startActivity(intent);
                }
        );
        binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recipesRecyclerView.setAdapter(recipeAdapter);
    }

    private void loadAllRecipes() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recipesRecyclerView.setVisibility(View.GONE);
        binding.noRecipesTextView.setVisibility(View.GONE);

        firebaseHelper.getRecipesRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allRecipes.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            try {
                                Recipe recipe = data.getValue(Recipe.class);
                                if (recipe != null) {
                                    allRecipes.add(recipe);
                                }
                            } catch (Exception e) {
                                // Skip recipes with parsing errors
                            }
                        }

                        binding.progressBar.setVisibility(View.GONE);

                        if (allRecipes.isEmpty()) {
                            binding.noRecipesTextView.setVisibility(View.VISIBLE);
                            binding.recipesRecyclerView.setVisibility(View.GONE);
                        } else {
                            binding.noRecipesTextView.setVisibility(View.GONE);
                            binding.recipesRecyclerView.setVisibility(View.VISIBLE);
                            recipeAdapter.updateRecipes(allRecipes);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(BrowseRecipesActivity.this,
                                "Error loading recipes: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}