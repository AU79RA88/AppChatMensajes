package com.example.firebaseapp;

public class Utils {
    public static String getConversationId(String a, String b) {
        if (a == null || b == null) return a + "_" + b;
        if (a.compareTo(b) < 0) return a + "_" + b;
        return b + "_" + a;
    }
}
