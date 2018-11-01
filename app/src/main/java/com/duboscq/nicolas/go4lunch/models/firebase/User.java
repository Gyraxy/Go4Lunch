package com.duboscq.nicolas.go4lunch.models.firebase;

import android.support.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    @Nullable private String urlPicture;
    @Nullable private String lunch;
    @Nullable private String lunch_date;
    @Nullable private String lunch_url;

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture, @Nullable String lunch, @Nullable String lunch_date, @Nullable String lunch_url) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.lunch = lunch;
        this.lunch_date = lunch_date;
        this.lunch_url = lunch_url;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public @Nullable String getUrlPicture() { return urlPicture; }
    public @Nullable String getLunch() { return lunch; }
    public @Nullable String getLunchDate() { return lunch_date; }
    public @Nullable String getLunchUrl() { return lunch_url; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }
    public void setLunch(@Nullable String lunch) { this.lunch = lunch; }
    public void setLunchDate(@Nullable String lunch_date) { this.lunch_date = lunch_date; }
    public void setLunchUrl(@Nullable String lunch_url) { this.lunch_url = lunch_url; }
}