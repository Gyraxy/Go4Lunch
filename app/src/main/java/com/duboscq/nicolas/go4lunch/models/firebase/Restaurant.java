package com.duboscq.nicolas.go4lunch.models.firebase;


public class Restaurant {

    private String restaurant_name;
    private int numberOfLikes;

    public Restaurant() { }

    public Restaurant(String restaurant, int numberOfLikes) {
        this.restaurant_name = restaurant_name;
        this.numberOfLikes = numberOfLikes;
    }


    // --- GETTERS ---
    public String getRestaurant() { return restaurant_name; }
    public int getLike() { return numberOfLikes; }

    // --- SETTERS ---
    public void setRestaurant(String restaurant_name) { this.restaurant_name = restaurant_name; }
    public void setLike(int numberOfLikes) { this.numberOfLikes = numberOfLikes; }

}
