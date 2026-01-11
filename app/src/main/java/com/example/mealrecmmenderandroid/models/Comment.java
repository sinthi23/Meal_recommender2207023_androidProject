package com.example.mealrecmmenderandroid.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Comment implements Serializable {
    private String commentId;
    private String recipeId;
    private String userId;
    private String userName;
    private String userEmail;
    private String commentText;
    private double rating;
    private long timestamp;
    private String parentCommentId;  // NEW - null if top-level comment
    private int replyCount;          // NEW - number of replies

    public Comment() {
        // Required empty constructor for Firebase
    }

    public Comment(String commentId, String recipeId, String userId, String userName,
                   String userEmail, String commentText, double rating, long timestamp) {
        this.commentId = commentId;
        this.recipeId = recipeId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.commentText = commentText;
        this.rating = rating;
        this.timestamp = timestamp;
        this.parentCommentId = null;  // Top-level comment
        this.replyCount = 0;
    }

    // Constructor for replies
    public Comment(String commentId, String recipeId, String userId, String userName,
                   String userEmail, String commentText, String parentCommentId, long timestamp) {
        this.commentId = commentId;
        this.recipeId = recipeId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.commentText = commentText;
        this.rating = 0;  // Replies don't have ratings
        this.timestamp = timestamp;
        this.parentCommentId = parentCommentId;
        this.replyCount = 0;
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public boolean isReply() {
        return parentCommentId != null && !parentCommentId.isEmpty();
    }
}