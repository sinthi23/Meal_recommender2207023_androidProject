package com.example.mealrecmmenderandroid.utils;

import android.util.Patterns;

public class ValidationHelper {

    public static String getEmailError(String email) {
        if (email == null || email.isEmpty()) {
            return "Email is required";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email format";
        }
        return null;
    }

    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        return null;
    }

    public static String getNameError(String name) {
        if (name == null || name.isEmpty()) {
            return "Name is required";
        }
        if (name.length() < 2) {
            return "Name must be at least 2 characters";
        }
        return null;
    }

    public static String getPhoneError(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "Phone is required";
        }
        if (phone.length() < 10) {
            return "Invalid phone number";
        }
        return null;
    }
}