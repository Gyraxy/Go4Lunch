package com.duboscq.nicolas.go4lunch;

import com.duboscq.nicolas.go4lunch.utils.DateUtility;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class UnitTest {

    @Test
    public void getWeekDayText(){
        List <String> weekhour_test = Arrays.asList( "Monday: 10:00 AM – 2:00 AM",
                "Tuesday: 10:00 AM – 2:00 AM",
                "Wednesday: 10:00 AM – 2:00 AM",
                "Thursday: 10:00 AM – 2:00 AM",
                "Friday: 10:00 AM – 2:00 AM",
                "Saturday: 10:00 AM – 2:00 AM",
                "Sunday: 10:00 AM – 2:00 AM");
        assertEquals("10:00 am – 2:00 am ", DateUtility.formatWeekDayText(weekhour_test));
    }
}
