package com.duboscq.nicolas.go4lunch.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtility {

    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String convertDateToHour(Date date){
        DateFormat dfTime = new SimpleDateFormat("dd/MM-HH:mm");
        return dfTime.format(date);
    }
}
