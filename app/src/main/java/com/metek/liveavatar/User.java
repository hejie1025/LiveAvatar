package com.metek.liveavatar;

public class User {
    private static final String TAG = User.class.getSimpleName();
    private static User ourInstance = new User();

    private String userId;

    public static User getInstance() {
        return ourInstance;
    }

    private User() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
