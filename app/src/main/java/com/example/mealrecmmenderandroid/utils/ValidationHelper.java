package com.example.mealrecmmenderandroid.utils;

import android.util.Patterns;

public class ValidationHelper {

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.length() >= 10;
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public static String getEmailError() {
        return "Please enter a valid email address";
    }

    public static String getPasswordError() {
        return "Password must be at least 6 characters";
    }

    public static String getNameError() {
        return "Name must be at least 2 characters";
    }

    public static String getPhoneError() {
        return "Please enter a valid phone number";
    }
}