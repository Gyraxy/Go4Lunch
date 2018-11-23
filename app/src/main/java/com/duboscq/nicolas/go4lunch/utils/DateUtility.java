package com.duboscq.nicolas.go4lunch.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    public static Integer dayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK)-1;
    }

    public static String formatWeekDayText(List<String> weekDayArray) {
        StringBuilder builder = new StringBuilder();
        if (weekDayArray.size() >=2) {
            String[] ot = weekDayArray.get(dayOfWeek()).split(" ");
            for (int i = 1, otLength = ot.length; i < otLength; i++) {
                String anOt = ot[i];
                builder.append(anOt).append(" ");
            }
        } else {
            builder.append("Closed");
        }

        return builder.toString().toLowerCase() ;
    }
}
