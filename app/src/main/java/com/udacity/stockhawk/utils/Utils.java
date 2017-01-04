package com.udacity.stockhawk.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;

import com.github.mikephil.charting.data.Entry;
import com.udacity.stockhawk.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public final class Utils {

    private Utils() {
    }

    // Gets stock display mode
    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    // Toggles the stock display mode
    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayMode = getDisplayMode(context);
        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }
        editor.apply();
    }

    // Creates a List of Entries based in the history string list returned from the content provider
    public static List<Entry> createEntryListFromString(String histStringList)
            throws NumberFormatException {
        ArrayList<Entry> entries = new ArrayList<>();
        if (!TextUtils.isEmpty(histStringList)) {
            String[] stocksArray = histStringList.split("\n");
            for (String stock : stocksArray) {
                if (!TextUtils.isEmpty(stock)) {
                    String[] stockInfo = stock.split("&");
                    if (stockInfo.length == 2) {
                        entries.add(new Entry(Float.valueOf(stockInfo[0]),
                                Float.valueOf(stockInfo[1])));
                    }
                }
            }
        }

        return entries;
    }

    // Formats a milliseconds timestamp in locale format without the time
    public static String formatMillisecondsForLocale(long timeInMilliseconds) {
        SimpleDateFormat dateInstance = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat
                .SHORT, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliseconds);
        String date = dateInstance.format(calendar.getTime());
        return date;
    }

    // Formats a milliseconds timestamp in locale format with the time
    public static String formatMillisecondsForLocaleWithTime(long timeInMilliseconds) {
        SimpleDateFormat dateInstance = (SimpleDateFormat) DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliseconds);
        String date = dateInstance.format(calendar.getTime());
        return date;
    }

    // Send system broadcast to update all stock hawk widgets
    public static void updateWidgets(Context context) {
        Intent updateWidgetsIntent = new Intent(Constants.Action.ACTION_UPDATE_WIDGETS)
                .setPackage(context.getPackageName());
        context.sendBroadcast(updateWidgetsIntent);
    }

    // Checks if phone has RTL enabled
    public static boolean isRTL(Context ctx) {
        Configuration config = ctx.getResources().getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        } else {
            return false;
        }
    }

    // Saves the last successful update from the Yahoo API
    public static void saveLastUpdate(Context context, long time) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.Pref.PREFERENCE_LAST_UPDATE, time);
        editor.commit();

    }

    // Gets the last saved update from shared preferences
    public static long getLastUpdate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(Constants.Pref.PREFERENCE_LAST_UPDATE, 0);
    }

    // Returns true if there's network connection available
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
