package com.duboscq.nicolas.go4lunch.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Nicolas DUBOSCQ on 28/09/2018
 */
public class SharedPreferencesUtility {

    private static SharedPreferences INSTANCE = null;

    public static SharedPreferences getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return INSTANCE;
    }

    public static String getString(Context context, String key){
        SharedPreferencesUtility.getInstance(context);
        return INSTANCE.getString(key,null);
    }

    public static int getInt(Context context, String key, int defaultvalue){
        SharedPreferencesUtility.getInstance(context);
        return INSTANCE.getInt(key,defaultvalue);
    }

    public static void putString(Context context, String key, String value){
        SharedPreferencesUtility.getInstance(context);
        INSTANCE.edit().putString(key, value).apply();
    }

    public static void putInt(Context context, String key, int value){
        SharedPreferencesUtility.getInstance(context);
        INSTANCE.edit().putInt(key, value).apply();
    }

}