package com.example.mealrecmmenderandroid.activities.provider;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealrecmmenderandroid.R;

public class EditRecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Recipe");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String recipeId = getIntent().getStringExtra("recipe_id");
        Toast.makeText(this, "Edit Recipe - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}