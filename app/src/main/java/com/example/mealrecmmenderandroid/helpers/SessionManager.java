package com.example.mealrecmmenderandroid.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "HealthyMealSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_ACCOUNT_TYPE = "account_type";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USERNAME = "username";  // NEW - for display name
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_PROFILE_IMAGE = "profile_image";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Create login session with all user details
     */
    public void createLoginSession(String userId, String userType, String userName, String email) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_ACCOUNT_TYPE, userType);  // For consistency
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USERNAME, userName);  // Store as username too
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Update user data (profile updates)
     */
    public void updateUserData(String userName, String profileImage) {
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USERNAME, userName);
        editor.putString(KEY_PROFILE_IMAGE, profileImage);
        editor.apply();
    }

    /**
     * Update only username
     */
    public void updateUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_NAME, username);
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    /**
     * Get user type (provider/consumer)
     */
    public String getUserType() {
        return pref.getString(KEY_USER_TYPE, null);
    }

    /**
     * Get account type with fallback
     */
    public String getAccountType() {
        String accountType = pref.getString(KEY_ACCOUNT_TYPE, null);
        if (accountType == null || accountType.isEmpty()) {
            return pref.getString(KEY_USER_TYPE, "consumer");
        }
        return accountType;
    }

    /**
     * Get username (display name)
     */
    public String getUserName() {
        String username = pref.getString(KEY_USERNAME, null);
        if (username == null || username.isEmpty()) {
            username = pref.getString(KEY_USER_NAME, null);
        }
        if (username == null || username.isEmpty()) {
            String email = getUserEmail();
            if (email != null && email.contains("@")) {
                username = email.split("@")[0];
            }
        }
        return username;
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Get profile image URL
     */
    public String getProfileImage() {
        return pref.getString(KEY_PROFILE_IMAGE, null);
    }

    /**
     * Check if user is a provider
     */
    public boolean isProvider() {
        String type = getAccountType();
        return type != null && type.equalsIgnoreCase("provider");
    }

    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        String type = getAccountType();
        return type != null && type.equalsIgnoreCase("admin");
    }

    /**
     * Check if user is a consumer
     */
    public boolean isConsumer() {
        String type = getAccountType();
        return type != null && type.equalsIgnoreCase("consumer");
    }

    /**
     * Logout user and clear session
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    /**
     * Clear all session data
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
