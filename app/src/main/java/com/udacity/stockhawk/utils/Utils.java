package com.udacity.stockhawk.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

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

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

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

    public static String formatMillisecondsForLocale(long timeInMilliseconds) {
        SimpleDateFormat dateInstance = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat
                .SHORT, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliseconds);
        String date = dateInstance.format(calendar.getTime());
        return date;
    }

    public static void updateWidgets(Context context) {
        Intent updateWidgetsIntent = new Intent(Constants.Action.ACTION_UPDATE_WIDGETS)
                .setPackage(context.getPackageName());
        context.sendBroadcast(updateWidgetsIntent);
    }
}
