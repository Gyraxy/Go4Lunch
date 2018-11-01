package com.duboscq.nicolas.go4lunch.models.firebase;

public class Restaurant {

    private int likes;

    public Restaurant() { }

    public Restaurant(int likes) {
        this.likes = likes;
    }


    // --- GETTERS ---

    public int getLikes() { return likes; }

    // --- SETTERS ---

    public void setLikes(int likes) { this.likes = likes; }

}
