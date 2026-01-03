package com.example.mealrecmmenderandroid.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Award implements Serializable {
    private String awardId;
    private String providerId;
    private String providerName;
    private String providerImageUrl;
    private int year;
    private int rank;
    private double averageRating;
    private int totalRecipes;
    private int totalRatings;
    private String prizeDetails;
    private String prizeAmount;
    private boolean isPrizeDelivered;
    private long awardDate;

    public Award() {
        this.awardDate = System.currentTimeMillis();
        this.isPrizeDelivered = false;
    }

    // Getters and Setters
    public String getAwardId() { return awardId; }
    public void setAwardId(String awardId) { this.awardId = awardId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderImageUrl() { return providerImageUrl; }
    public void setProviderImageUrl(String providerImageUrl) {
        this.providerImageUrl = providerImageUrl;
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getTotalRecipes() { return totalRecipes; }
    public void setTotalRecipes(int totalRecipes) { this.totalRecipes = totalRecipes; }

    public int getTotalRatings() { return totalRatings; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }

    public String getPrizeDetails() { return prizeDetails; }
    public void setPrizeDetails(String prizeDetails) { this.prizeDetails = prizeDetails; }

    public String getPrizeAmount() { return prizeAmount; }
    public void setPrizeAmount(String prizeAmount) { this.prizeAmount = prizeAmount; }

    public boolean isPrizeDelivered() { return isPrizeDelivered; }
    public void setPrizeDelivered(boolean prizeDelivered) { isPrizeDelivered = prizeDelivered; }

    public long getAwardDate() { return awardDate; }
    public void setAwardDate(long awardDate) { this.awardDate = awardDate; }

    public String getRankDisplay() {
        switch (rank) {
            case 1: return "🥇 1st Place";
            case 2: return "🥈 2nd Place";
            case 3: return "🥉 3rd Place";
            default: return String.valueOf(rank);
        }
    }
}