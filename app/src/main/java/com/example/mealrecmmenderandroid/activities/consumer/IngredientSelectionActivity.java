package com.example.mealrecmmenderandroid.activities.consumer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.adapters.IngredientAdapter;
import com.example.mealrecmmenderandroid.databinding.ActivityIngredientSelectionBinding;
import com.example.mealrecmmenderandroid.models.Ingredient;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IngredientSelectionActivity extends AppCompatActivity {

    private ActivityIngredientSelectionBinding binding;
    private FirebaseHelper firebaseHelper;
    private IngredientAdapter adapter;
    private List<Ingredient> allIngredients;
    private List<Ingredient> filteredIngredients;
    private Set<String> selectedIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIngredientSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        allIngredients = new ArrayList<>();
        filteredIngredients = new ArrayList<>();

        // Get previously selected ingredients
        ArrayList<String> previouslySelected =
                getIntent().getStringArrayListExtra("selected_ingredients");
        selectedIngredients = previouslySelected != null ?
                new HashSet<>(previouslySelected) : new HashSet<>();

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadIngredients();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Ingredients");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new IngredientAdapter(this, filteredIngredients,
                selectedIngredients, (ingredient, isSelected) -> {
            updateSelectedCount();
        });
        binding.ingredientsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.ingredientsRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterIngredients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.doneButton.setOnClickListener(v -> finishSelection());

        binding.clearButton.setOnClickListener(v -> {
            selectedIngredients.clear();
            adapter.notifyDataSetChanged();
            updateSelectedCount();
        });
    }

    private void loadIngredients() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.getIngredientsRef()
                .orderByChild("name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allIngredients.clear();
                        for (DataSnapshot ingredientSnapshot : snapshot.getChildren()) {
                            Ingredient ingredient = ingredientSnapshot.getValue(Ingredient.class);
                            if (ingredient != null) {
                                allIngredients.add(ingredient);
                            }
                        }
                        filteredIngredients.clear();
                        filteredIngredients.addAll(allIngredients);
                        adapter.updateIngredients(filteredIngredients);
                        binding.progressBar.setVisibility(View.GONE);
                        updateSelectedCount();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(IngredientSelectionActivity.this,
                                "Error loading ingredients: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterIngredients(String query) {
        filteredIngredients.clear();

        if (query.isEmpty()) {
            filteredIngredients.addAll(allIngredients);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Ingredient ingredient : allIngredients) {
                if (ingredient.getName().toLowerCase().contains(lowerQuery) ||
                        ingredient.getCategory().toLowerCase().contains(lowerQuery)) {
                    filteredIngredients.add(ingredient);
                }
            }
        }

        adapter.updateIngredients(filteredIngredients);
    }

    private void updateSelectedCount() {
        int count = selectedIngredients.size();
        binding.selectedCountTextView.setText(count + " selected");
        binding.doneButton.setEnabled(count > 0);
    }

    private void finishSelection() {
        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra("selected_ingredients",
                new ArrayList<>(selectedIngredients));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}