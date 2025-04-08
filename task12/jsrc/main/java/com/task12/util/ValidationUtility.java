package com.task12.util;

public class ValidationUtility {
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 12 &&
               password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$%^*-_])[A-Za-z\\d$%^*-_]+$");
    }
}
