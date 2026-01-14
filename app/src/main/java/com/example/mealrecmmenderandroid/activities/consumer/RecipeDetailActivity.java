package com.example.mealrecmmenderandroid.activities.consumer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.adapters.CommentAdapter;
import com.example.mealrecmmenderandroid.databinding.ActivityRecipeDetailBinding;
import com.example.mealrecmmenderandroid.models.Comment;
import com.example.mealrecmmenderandroid.models.CookHistory;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.utils.ImageHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {

    private ActivityRecipeDetailBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private Recipe currentRecipe;
    private String recipeId;


    private CommentAdapter commentAdapter;
    private List<Comment> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);
        commentsList = new ArrayList<>();

        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId == null) {
            recipeId = getIntent().getStringExtra("recipe_id");
        }

        if (recipeId == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupListeners();
        setupCommentsSection();
        loadRecipe();
        loadComments();
        incrementViewCount();
        setupRealtimeCountsListener();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        binding.saveToCookHistoryButton.setOnClickListener(v -> showSaveToCookHistoryDialog());
        binding.shareButton.setOnClickListener(v -> shareRecipe());
    }

    private void setupCommentsSection() {
        CommentAdapter.OnCommentInteractionListener listener = new CommentAdapter.OnCommentInteractionListener() {
            @Override
            public void onReplyClick(Comment comment) {
                showReplyDialog(comment);
            }

            @Override
            public void onViewRepliesClick(Comment comment) {
                showRepliesDialog(comment);
            }
        };

        commentAdapter = new CommentAdapter(this, commentsList, listener);
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.commentsRecyclerView.setAdapter(commentAdapter);
        binding.commentsRecyclerView.setNestedScrollingEnabled(false);

        binding.submitCommentButton.setOnClickListener(v -> submitComment());
    }

    private void setupRealtimeCountsListener() {
        firebaseHelper.getRecipeRef(recipeId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Integer viewCount = snapshot.child("viewCount").getValue(Integer.class);
                            Integer cookCount = snapshot.child("cookCount").getValue(Integer.class);

                            if (viewCount == null) viewCount = 0;
                            if (cookCount == null) cookCount = 0;

                            binding.viewCountTextView.setText(viewCount + (viewCount == 1 ? " view" : " views"));
                            binding.cookCountTextView.setText(cookCount + (cookCount == 1 ? " time cooked" : " times cooked"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("RecipeDetail", "Failed to load counts: " + error.getMessage());
                    }
                });
    }

    private void loadComments() {
        firebaseHelper.getRecipeCommentsRef(recipeId)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentsList.clear();
                        for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                            Comment comment = commentSnapshot.getValue(Comment.class);
                            if (comment != null && !comment.isReply()) {  // Only top-level comments
                                commentsList.add(comment);
                            }
                        }
                        Collections.reverse(commentsList);
                        commentAdapter.updateComments(commentsList);

                        binding.commentsCountTextView.setText(commentsList.size() + " Comment" +
                                (commentsList.size() != 1 ? "s" : ""));

                        if (commentsList.isEmpty()) {
                            binding.commentsRecyclerView.setVisibility(View.GONE);
                            binding.noCommentsTextView.setVisibility(View.VISIBLE);
                        } else {
                            binding.commentsRecyclerView.setVisibility(View.VISIBLE);
                            binding.noCommentsTextView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RecipeDetailActivity.this,
                                "Error loading comments: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitComment() {
        String commentText = binding.commentEditText.getText().toString().trim();
        float rating = binding.commentRatingBar.getRating();

        if (TextUtils.isEmpty(commentText)) {
            binding.commentEditText.setError("Please write a comment");
            return;
        }

        if (rating == 0) {
            Toast.makeText(this, "Please add a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUserId();
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();

        String commentId = firebaseHelper.getRecipeCommentsRef(recipeId).push().getKey();

        if (commentId == null) {
            Toast.makeText(this, "Failed to submit comment", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment comment = new Comment(
                commentId,
                recipeId,
                userId,
                userName != null ? userName : userEmail,
                userEmail,
                commentText,
                rating,
                System.currentTimeMillis()
        );

        firebaseHelper.getRecipeCommentsRef(recipeId)
                .child(commentId)
                .setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show();
                    binding.commentEditText.setText("");
                    binding.commentRatingBar.setRating(0);

                    updateRecipeRating(rating);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post comment: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showReplyDialog(Comment parentComment) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_reply_comment);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextView originalCommentText = dialog.findViewById(R.id.originalCommentText);
        EditText replyEditText = dialog.findViewById(R.id.replyEditText);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);
        MaterialButton postReplyButton = dialog.findViewById(R.id.postReplyButton);

        originalCommentText.setText("Replying to: " + parentComment.getCommentText());

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        postReplyButton.setOnClickListener(v -> {
            String replyText = replyEditText.getText().toString().trim();
            if (TextUtils.isEmpty(replyText)) {
                replyEditText.setError("Please write a reply");
                return;
            }
            postReply(parentComment, replyText);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void postReply(Comment parentComment, String replyText) {
        String userId = sessionManager.getUserId();
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();

        String replyId = firebaseHelper.getRecipeCommentsRef(recipeId).push().getKey();

        if (replyId == null) {
            Toast.makeText(this, "Failed to post reply", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment reply = new Comment(
                replyId,
                recipeId,
                userId,
                userName != null ? userName : userEmail,
                userEmail,
                replyText,
                parentComment.getCommentId(),
                System.currentTimeMillis()
        );

        firebaseHelper.getRecipeCommentsRef(recipeId)
                .child(replyId)
                .setValue(reply)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Reply posted!", Toast.LENGTH_SHORT).show();

                    incrementReplyCount(parentComment.getCommentId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post reply: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void incrementReplyCount(String parentCommentId) {
        firebaseHelper.getRecipeCommentsRef(recipeId)
                .child(parentCommentId)
                .child("replyCount")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue + 1);
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        if (error != null) {
                            android.util.Log.e("RecipeDetail", "Reply count update failed: " + error.getMessage());
                        }
                    }
                });
    }

    private void showRepliesDialog(Comment parentComment) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_view_replies);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        TextView titleTextView = dialog.findViewById(R.id.titleTextView);
        RecyclerView repliesRecyclerView = dialog.findViewById(R.id.repliesRecyclerView);
        MaterialButton closeButton = dialog.findViewById(R.id.closeButton);

        titleTextView.setText(parentComment.getReplyCount() +
                (parentComment.getReplyCount() == 1 ? " Reply" : " Replies"));

        List<Comment> replies = new ArrayList<>();
        CommentAdapter repliesAdapter = new CommentAdapter(this, replies,
                new CommentAdapter.OnCommentInteractionListener() {
                    @Override
                    public void onReplyClick(Comment comment) {
                        dialog.dismiss();
                        showReplyDialog(parentComment);
                    }

                    @Override
                    public void onViewRepliesClick(Comment comment) {
                    }
                });

        repliesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        repliesRecyclerView.setAdapter(repliesAdapter);
        firebaseHelper.getRecipeCommentsRef(recipeId)
                .orderByChild("parentCommentId")
                .equalTo(parentComment.getCommentId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        replies.clear();
                        for (DataSnapshot replySnapshot : snapshot.getChildren()) {
                            Comment reply = replySnapshot.getValue(Comment.class);
                            if (reply != null) {
                                replies.add(reply);
                            }
                        }
                        repliesAdapter.updateComments(replies);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RecipeDetailActivity.this,
                                "Error loading replies: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadRecipe() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.getRecipeRef(recipeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                currentRecipe = snapshot.getValue(Recipe.class);
                                if (currentRecipe != null) {
                                    displayRecipe(currentRecipe);
                                } else {
                                    Toast.makeText(RecipeDetailActivity.this,
                                            "Error loading recipe data", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } catch (Exception e) {
                                android.util.Log.e("RecipeDetail", "Error parsing recipe: " + e.getMessage());
                                e.printStackTrace();
                                Toast.makeText(RecipeDetailActivity.this,
                                        "Error loading recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(RecipeDetailActivity.this,
                                    "Recipe not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(RecipeDetailActivity.this,
                                "Error loading recipe: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayRecipe(Recipe recipe) {
        binding.recipeNameTextView.setText(recipe.getRecipeName() != null ? recipe.getRecipeName() : "Unknown Recipe");
        binding.descriptionTextView.setText(recipe.getDescription() != null ? recipe.getDescription() : "No description");
        binding.providerNameTextView.setText("By " + (recipe.getProviderName() != null ? recipe.getProviderName() : "Unknown"));

        binding.caloriesTextView.setText(recipe.getCalories() + " cal");
        binding.prepTimeTextView.setText(recipe.getPreparationTime() + " min");
        binding.cookTimeTextView.setText(recipe.getCookingTime() + " min");
        binding.servingsTextView.setText(recipe.getServings() + " servings");
        binding.healthScoreTextView.setText(String.format("%.0f%%", recipe.getHealthScore()));
        binding.difficultyTextView.setText(recipe.getDifficulty() != null ? recipe.getDifficulty() : "Medium");
        binding.categoryTextView.setText(recipe.getCategory() != null ? recipe.getCategory() : "");
        binding.cuisineTextView.setText(recipe.getCuisine() != null ? recipe.getCuisine() : "");

        binding.ratingBar.setRating((float) recipe.getAverageRating());
        binding.ratingCountTextView.setText("(" + recipe.getTotalRatings() + " ratings)");

        binding.viewCountTextView.setText("0 views");
        binding.cookCountTextView.setText("0 times cooked");

        binding.proteinTextView.setText(String.format("%.1fg", recipe.getProtein()));
        binding.carbsTextView.setText(String.format("%.1fg", recipe.getCarbs()));
        binding.fatTextView.setText(String.format("%.1fg", recipe.getFat()));
        binding.fiberTextView.setText(String.format("%.1fg", recipe.getFiber()));

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            ImageHelper.loadImage(this, recipe.getImageUrl(), binding.recipeImageView);
        } else {
            binding.recipeImageView.setImageResource(R.drawable.ic_image_placeholder);
        }

        displayIngredients(recipe);

        binding.instructionsTextView.setText(recipe.getInstructions() != null ? recipe.getInstructions() : "No instructions provided");

        displayTags(null);
    }

    private void displayIngredients(Recipe recipe) {
        binding.ingredientsLayout.removeAllViews();

        try {
            Map<String, Recipe.IngredientDetail> detailsMap = recipe.getIngredientDetailsMap();

            if (detailsMap != null && !detailsMap.isEmpty()) {
                for (Map.Entry<String, Recipe.IngredientDetail> entry : detailsMap.entrySet()) {
                    Recipe.IngredientDetail detail = entry.getValue();
                    if (detail != null) {
                        addIngredientView(detail.getName(), detail.getQuantity(), detail.getUnit());
                    }
                }
            } else {
                Map<String, String> ingredientsMap = recipe.getIngredientsMap();

                if (ingredientsMap != null && !ingredientsMap.isEmpty()) {
                    for (Map.Entry<String, String> entry : ingredientsMap.entrySet()) {
                        addIngredientView(entry.getValue(), "", "");
                    }
                } else {
                    List<String> ingredientsList = recipe.getIngredientsList();

                    if (ingredientsList != null && !ingredientsList.isEmpty()) {
                        for (String ingredient : ingredientsList) {
                            if (ingredient != null && !ingredient.isEmpty()) {
                                addIngredientView(ingredient, "", "");
                            }
                        }
                    } else {
                        addIngredientView("No ingredients listed", "", "");
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("RecipeDetail", "Error displaying ingredients: " + e.getMessage());
            e.printStackTrace();
            addIngredientView("Error loading ingredients", "", "");
        }
    }

    private void addIngredientView(String name, String quantity, String unit) {
        View ingredientView = getLayoutInflater().inflate(
                R.layout.item_ingredient_detail, binding.ingredientsLayout, false);

        androidx.appcompat.widget.AppCompatTextView ingredientText =
                ingredientView.findViewById(R.id.ingredientTextView);

        String displayText = name;
        if (quantity != null && !quantity.isEmpty() && unit != null && !unit.isEmpty()) {
            displayText = quantity + " " + unit + " " + name;
        }
        ingredientText.setText("• " + displayText);

        binding.ingredientsLayout.addView(ingredientView);
    }

    private void displayTags(List<String> tags) {
        binding.tagsChipGroup.removeAllViews();

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                com.google.android.material.chip.Chip chip =
                        new com.google.android.material.chip.Chip(this);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.chip_background);
                chip.setTextColor(getResources().getColor(R.color.primary_color));
                binding.tagsChipGroup.addView(chip);
            }
        }
    }

    private void showSaveToCookHistoryDialog() {
        if (currentRecipe == null) {
            Toast.makeText(this, "Recipe not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_save_cook_history);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
        }

        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        EditText notesEditText = dialog.findViewById(R.id.notesEditText);
        EditText servingsEditText = dialog.findViewById(R.id.servingsEditText);
        MaterialButton saveButton = dialog.findViewById(R.id.saveButton);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);

        servingsEditText.setText(String.valueOf(currentRecipe.getServings()));

        saveButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String notes = notesEditText.getText().toString().trim();
            String servingsStr = servingsEditText.getText().toString().trim();
            int servings = servingsStr.isEmpty() ? 1 : Integer.parseInt(servingsStr);

            saveToCookHistory(rating, notes, servings);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveToCookHistory(float rating, String notes, int servings) {
        String userId = sessionManager.getUserId();
        String historyId = firebaseHelper.getUserCookHistoryRef(userId).push().getKey();

        if (historyId == null) {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
            return;
        }

        CookHistory cookHistory = new CookHistory(
                historyId,
                userId,
                currentRecipe.getRecipeId(),
                currentRecipe.getRecipeName(),
                currentRecipe.getCalories(),
                System.currentTimeMillis(),
                rating,
                notes
        );

        firebaseHelper.getUserCookHistoryRef(userId)
                .child(historyId)
                .setValue(cookHistory)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Saved to cook history!", Toast.LENGTH_SHORT).show();
                    updateRecipeRating(rating);
                    incrementCookCount();
                    if (rating > 0) {
                        updateProviderRating(rating);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRecipeRating(float newRating) {
        if (newRating == 0) return;

        String userId = sessionManager.getUserId();

        firebaseHelper.getRecipeRef(recipeId)
                .child("userRatings")
                .child(userId)
                .setValue((double) newRating);

        firebaseHelper.getRecipeRef(recipeId)
                .child("userRatings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double sum = 0;
                        int count = 0;
                        for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                            Double rating = ratingSnapshot.getValue(Double.class);
                            if (rating != null) {
                                sum += rating;
                                count++;
                            }
                        }

                        double newAverage = count > 0 ? sum / count : 0;

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("averageRating", newAverage);
                        updates.put("totalRatings", count);

                        firebaseHelper.getRecipeRef(recipeId).updateChildren(updates);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("RecipeDetail", "Rating update cancelled: " + error.getMessage());
                    }
                });
    }

    private void incrementCookCount() {
        firebaseHelper.getRecipeRef(recipeId)
                .child("cookCount")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue + 1);
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        if (error != null) {
                            android.util.Log.e("RecipeDetail", "Cook count update failed: " + error.getMessage());
                        }
                    }
                });
    }

    private void incrementViewCount() {
        firebaseHelper.getRecipeRef(recipeId)
                .child("viewCount")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue + 1);
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        if (error != null) {
                            android.util.Log.e("RecipeDetail", "View count update failed: " + error.getMessage());
                        }
                    }
                });
    }

    private void updateProviderRating(float rating) {
        String providerId = currentRecipe.getProviderId();
        if (providerId == null) return;

        firebaseHelper.getUserRef(providerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Double currentTotal = snapshot.child("totalRating").getValue(Double.class);
                            Integer currentCount = snapshot.child("ratingCount").getValue(Integer.class);

                            if (currentTotal == null) currentTotal = 0.0;
                            if (currentCount == null) currentCount = 0;

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("totalRating", currentTotal + rating);
                            updates.put("ratingCount", currentCount + 1);
                            updates.put("averageRating", (currentTotal + rating) / (currentCount + 1));

                            firebaseHelper.getUserRef(providerId).updateChildren(updates);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("RecipeDetail", "Provider rating update cancelled: " + error.getMessage());
                    }
                });
    }

    private void shareRecipe() {
        if (currentRecipe == null) {
            Toast.makeText(this, "Recipe not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalTime = currentRecipe.getPreparationTime() + currentRecipe.getCookingTime();

        String shareText = "Check out this recipe: " + currentRecipe.getRecipeName() +
                "\nCalories: " + currentRecipe.getCalories() +
                "\nTime: " + totalTime + " mins" +
                "\n\nHealthy Meal Recommender App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Recipe"));
    }
}