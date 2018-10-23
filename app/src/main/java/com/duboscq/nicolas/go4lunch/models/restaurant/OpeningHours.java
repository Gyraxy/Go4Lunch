package com.duboscq.nicolas.go4lunch.models.restaurant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OpeningHours {

    // Variables
    @SerializedName("open_now")
    @Expose
    private Boolean openNow;

    @SerializedName("weekday_text")
    @Expose
    private List<Object> weekdayText = new ArrayList<Object>();

    // Constructor
    public OpeningHours(Boolean openNow, List<Object> weekdayText) {
        this.openNow = openNow;
        this.weekdayText = weekdayText;
    }

    // Getters & setters

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public List<Object> getWeekdayText() {
        return weekdayText;
    }

    public void setWeekdayText(List<Object> weekdayText) {
        this.weekdayText = weekdayText;
    }


}