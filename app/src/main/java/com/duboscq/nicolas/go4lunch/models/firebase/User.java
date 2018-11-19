package com.duboscq.nicolas.go4lunch.models.firebase;

import android.support.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    @Nullable private String urlPicture;
    @Nullable private String lunch_id;
    @Nullable private String lunch_name;
    @Nullable private String lunch_date;

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture, @Nullable String lunch_id, @Nullable String lunch_name, @Nullable String lunch_date) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.lunch_id = lunch_id;
        this.lunch_name = lunch_name;
        this.lunch_date = lunch_date;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public @Nullable String getUrlPicture() { return urlPicture; }
    public @Nullable String getLunchId() { return lunch_id; }
    public @Nullable String getLunchName() { return lunch_name; }
    public @Nullable String getLunchDate() { return lunch_date; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }
    public void setLunchId(@Nullable String lunch_id) { this.lunch_id = lunch_id; }
    public void setLunchName(@Nullable String lunch_name) { this.lunch_name = lunch_name; }
    public void setLunchDate(@Nullable String lunch_date) { this.lunch_date = lunch_date; }
}