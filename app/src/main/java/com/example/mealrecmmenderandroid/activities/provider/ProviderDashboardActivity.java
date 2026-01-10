package com.example.mealrecmmenderandroid.activities.provider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.adapters.ProviderRecipeAdapter;
import com.example.mealrecmmenderandroid.databinding.ActivityProviderDashboardBinding;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.models.User;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ProviderDashboardActivity extends AppCompatActivity {

    private ActivityProviderDashboardBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private ProviderRecipeAdapter adapter;
    private List<Recipe> myRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProviderDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        myRecipes = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadProviderStats();
        loadMyRecipes();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Provider Dashboard");

            // Show user email in subtitle
            String userEmail = sessionManager.getUserEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                getSupportActionBar().setSubtitle(userEmail);
            }
        }
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ProviderRecipeAdapter(this, myRecipes,
                new ProviderRecipeAdapter.OnRecipeActionListener() {
                    @Override
                    public void onEditClick(Recipe recipe) {
                        Intent intent = new Intent(ProviderDashboardActivity.this,
                                EditRecipeActivity.class);
                        intent.putExtra("recipe_id", recipe.getRecipeId());
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(Recipe recipe) {
                        deleteRecipe(recipe);
                    }

                    @Override
                    public void onViewClick(Recipe recipe) {
                        // View recipe details
                        Intent intent = new Intent(ProviderDashboardActivity.this,
                                com.example.mealrecmmenderandroid.activities.consumer.RecipeDetailActivity.class);
                        intent.putExtra("recipeId", recipe.getRecipeId());
                        startActivity(intent);
                    }
                });

        binding.recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recipesRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProviderDashboardActivity.this, AddRecipeActivity.class));
            }
        });
    }

    private void loadProviderStats() {
        String providerId = sessionManager.getUserId();
        String userEmail = sessionManager.getUserEmail();

        android.util.Log.d("ProviderDashboard", "===== CURRENT USER INFO =====");
        android.util.Log.d("ProviderDashboard", "User ID: " + providerId);
        android.util.Log.d("ProviderDashboard", "Email: " + userEmail);
        android.util.Log.d("ProviderDashboard", "=============================");

        if (providerId == null || providerId.isEmpty()) {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper.getUserRef(providerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                User provider = snapshot.getValue(User.class);
                                if (provider != null) {
                                    binding.totalRecipesTextView.setText(
                                            String.valueOf(provider.getTotalRecipesProvided()));
                                    binding.averageRatingTextView.setText(
                                            String.format("%.1f", provider.getAverageRating()));
                                    binding.totalRatingsTextView.setText(
                                            String.valueOf(provider.getRatingCount()));
                                } else {
                                    setDefaultStats();
                                }
                            } catch (Exception e) {
                                android.util.Log.e("ProviderDashboard", "Error loading stats: " + e.getMessage());
                                e.printStackTrace();
                                setDefaultStats();
                            }
                        } else {
                            setDefaultStats();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ProviderDashboard", "Stats query cancelled: " + error.getMessage());
                        setDefaultStats();
                    }
                });
    }

    private void setDefaultStats() {
        binding.totalRecipesTextView.setText("0");
        binding.averageRatingTextView.setText("0.0");
        binding.totalRatingsTextView.setText("0");
    }

    private void loadMyRecipes() {
        binding.progressBar.setVisibility(View.VISIBLE);

        String providerId = sessionManager.getUserId();
        String userEmail = sessionManager.getUserEmail();

        // Show debug info
        android.util.Log.d("ProviderDashboard", "===== LOADING RECIPES =====");
        android.util.Log.d("ProviderDashboard", "Searching for providerId: " + providerId);
        android.util.Log.d("ProviderDashboard", "User Email: " + userEmail);

        if (providerId == null || providerId.isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User ID is null or empty!", Toast.LENGTH_LONG).show();
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.recipesRecyclerView.setVisibility(View.GONE);
            return;
        }

        Query query = firebaseHelper.getRecipesRef()
                .orderByChild("providerId")
                .equalTo(providerId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myRecipes.clear();

                android.util.Log.d("ProviderDashboard", "Total recipes found: " + snapshot.getChildrenCount());

                try {
                    int successCount = 0;
                    int errorCount = 0;

                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        try {
                            Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                            if (recipe != null) {
                                android.util.Log.d("ProviderDashboard", "✓ Loaded recipe: " + recipe.getRecipeName());
                                myRecipes.add(recipe);
                                successCount++;
                            } else {
                                android.util.Log.w("ProviderDashboard", "✗ Recipe is null for key: " + recipeSnapshot.getKey());
                                errorCount++;
                            }
                        } catch (Exception e) {
                            android.util.Log.e("ProviderDashboard", "✗ Error parsing recipe: " + e.getMessage());
                            e.printStackTrace();
                            errorCount++;
                        }
                    }

                    android.util.Log.d("ProviderDashboard", "===== LOAD SUMMARY =====");
                    android.util.Log.d("ProviderDashboard", "Successfully loaded: " + successCount);
                    android.util.Log.d("ProviderDashboard", "Errors: " + errorCount);
                    android.util.Log.d("ProviderDashboard", "Total in list: " + myRecipes.size());
                    android.util.Log.d("ProviderDashboard", "========================");

                    if (myRecipes.isEmpty()) {
                        binding.emptyStateLayout.setVisibility(View.VISIBLE);
                        binding.recipesRecyclerView.setVisibility(View.GONE);

                        android.util.Log.i("ProviderDashboard", "No recipes found for this user");

                        // Show helpful message
                        Toast.makeText(ProviderDashboardActivity.this,
                                "No recipes yet. Click 'Add Recipe' to create your first recipe!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        binding.emptyStateLayout.setVisibility(View.GONE);
                        binding.recipesRecyclerView.setVisibility(View.VISIBLE);

                        android.util.Log.i("ProviderDashboard", "Displaying " + myRecipes.size() + " recipe(s)");
                    }

                    adapter.updateRecipes(myRecipes);
                } catch (Exception e) {
                    android.util.Log.e("ProviderDashboard", "Fatal error in onDataChange: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(ProviderDashboardActivity.this,
                            "Error processing recipes: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                } finally {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("ProviderDashboard", "Database query cancelled: " + error.getMessage());
                binding.progressBar.setVisibility(View.GONE);

                Toast.makeText(ProviderDashboardActivity.this,
                        "Error loading recipes: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();

                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.recipesRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void deleteRecipe(Recipe recipe) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete '" + recipe.getRecipeName() + "'?")
                .setPositiveButton("Delete", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        binding.progressBar.setVisibility(View.VISIBLE);

                        firebaseHelper.getRecipeRef(recipe.getRecipeId())
                                .removeValue()
                                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        android.util.Log.d("ProviderDashboard", "Recipe deleted: " + recipe.getRecipeId());
                                        Toast.makeText(ProviderDashboardActivity.this,
                                                "Recipe deleted successfully",
                                                Toast.LENGTH_SHORT).show();
                                        updateProviderRecipeCount(-1);
                                        binding.progressBar.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        android.util.Log.e("ProviderDashboard", "Delete failed: " + e.getMessage());
                                        Toast.makeText(ProviderDashboardActivity.this,
                                                "Failed to delete: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        binding.progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProviderRecipeCount(int change) {
        String providerId = sessionManager.getUserId();

        firebaseHelper.getUserRef(providerId)
                .child("totalRecipesProvided")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(change > 0 ? 1 : 0);
                        } else {
                            mutableData.setValue(Math.max(0, currentValue + change));
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(com.google.firebase.database.DatabaseError error,
                                           boolean committed,
                                           com.google.firebase.database.DataSnapshot snapshot) {
                        if (error != null) {
                            android.util.Log.e("ProviderDashboard", "Transaction failed: " + error.getMessage());
                        } else {
                            android.util.Log.d("ProviderDashboard", "Recipe count updated: " + change);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to dashboard
        android.util.Log.d("ProviderDashboard", "onResume() - Reloading data");
        loadProviderStats();
        loadMyRecipes();
    }
}