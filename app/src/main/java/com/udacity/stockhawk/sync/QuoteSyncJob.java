package com.udacity.stockhawk.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.Constants;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final String JOB_TAG_ONE_OFF = "job_tag_one_off";
    private static final String JOB_TAG_PERIODIC = "job_tag_periodic";
    private static final int PERIOD_SYNC = 30;
    private static final int PERIOD_HISTORY = 1;

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -PERIOD_HISTORY);

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(Contract.Quote.URI, null, null, null, null);
            ArrayList<String> stocksArrayList = new ArrayList<>();

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote
                            .COLUMN_SYMBOL));
                    int type = cursor.getInt(cursor.getColumnIndex(Contract.Quote
                            .COLUMN_TYPE));
                    switch (type) {
                        case Constants.StockType.LOADING:
                            Stock stock = YahooFinance.get(symbol);
                            // Unknown stock
                            if (stock == null || stock.getName() == null) {
                                ContentValues quoteCV = new ContentValues();
                                quoteCV.put(Contract.Quote.COLUMN_TYPE, Constants.StockType
                                        .UNKNOWN);
                                context.getContentResolver().update(Contract.Quote.URI, quoteCV,
                                        Contract.Quote.COLUMN_SYMBOL + "=?", new String[]{symbol});
                            } else {
                                stocksArrayList.add(symbol);
                            }
                            break;
                        case Constants.StockType.UNKNOWN:
                            // Do not add symbol on query list
                            break;
                        case Constants.StockType.KNOWN:
                            stocksArrayList.add(symbol);
                            break;
                    }
                }

                String[] stocksArray = stocksArrayList.toArray(new String[stocksArrayList.size()]);
                Map<String, Stock> quotes = YahooFinance.get(stocksArray, from, to, Interval
                        .WEEKLY);
                for (String symbol : stocksArray) {
                    ContentValues quoteCV = new ContentValues();
                    Stock stock = quotes.get(symbol);
                    StockQuote quote = stock.getQuote();
                    BigDecimal price = quote.getPrice();
                    BigDecimal change = quote.getChange();
                    BigDecimal percentChange = quote.getChangeInPercent();

                    List<HistoricalQuote> history = stock.getHistory();
                    StringBuilder historyBuilder = new StringBuilder();

                    for (HistoricalQuote it : history) {
                        historyBuilder.append(it.getDate().getTimeInMillis());
                        historyBuilder.append("&");
                        historyBuilder.append(it.getClose().floatValue());
                        historyBuilder.append("\n");
                    }
                    quoteCV.put(Contract.Quote.COLUMN_TYPE, Constants.StockType.KNOWN);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price.floatValue());
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, change
                            .floatValue());
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, percentChange
                            .floatValue());
                    quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                    context.getContentResolver().update(Contract.Quote.URI, quoteCV,
                            Contract.Quote.COLUMN_SYMBOL + "=?", new String[]{symbol});
                }


            }
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(Constants.Action.ACTION_SYNC_END);
            broadcastIntent.putExtra(Constants.Extra.EXTRA_SYNC_RESULT_TYPE,
                    Constants.SyncResultType.RESULT_SUCCESS);
            context.sendBroadcast(broadcastIntent);
        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(Constants.Action.ACTION_SYNC_END);
            broadcastIntent.putExtra(Constants.Extra.EXTRA_SYNC_RESULT_TYPE,
                    Constants.SyncResultType.RESULT_ERROR);
            context.sendBroadcast(broadcastIntent);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic sync every " + PERIOD_SYNC + " seconds");

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job myJob = dispatcher.newJobBuilder()
                .setService(QuoteJobService.class)
                .setTag(JOB_TAG_PERIODIC)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(PERIOD_SYNC, PERIOD_SYNC))
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(true)
                .build();

        dispatcher.mustSchedule(myJob);
    }

    synchronized public static void syncImmediately(Context context) {
        Timber.d("Scheduling a immediate sync");

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver
                (context));

        Bundle bundle = new Bundle();
        Job myJob = dispatcher.newJobBuilder()
                .setService(QuoteJobService.class)
                .setTag(JOB_TAG_ONE_OFF)
                .setExtras(bundle)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(0, 0))
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(true)
                .build();

        dispatcher.mustSchedule(myJob);

    }

    synchronized public static void initializeSyncJob(final Context context) {
        schedulePeriodic(context);
        syncImmediately(context);
    }

    synchronized public static void stopSyncJob(final Context context) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver
                (context));

        dispatcher.cancel(JOB_TAG_ONE_OFF);
        dispatcher.cancel(JOB_TAG_PERIODIC);
    }
}
