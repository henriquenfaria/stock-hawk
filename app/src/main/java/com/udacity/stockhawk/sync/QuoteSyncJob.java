package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    static final int ONE_OFF_ID = 2;
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -2);

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(Contract.Quote.uri, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);

                    boolean isUnknown = !cursor.isNull(Contract.Quote.POSITION_IS_UNKNOWN)
                            && cursor.getInt(Contract.Quote.POSITION_IS_UNKNOWN) == Utils
                            .UNKNOWN_STOCK;

                    // Stock is available/known and we should update it, otherwise skip it
                    if (!isUnknown) {



                        // TODO: Bulk query as before? Is it faster?
                        Stock stock = YahooFinance.get(symbol);
                        StockQuote quote = stock.getQuote();
                        BigDecimal price = quote.getPrice();
                        BigDecimal ask = quote.getAsk();

                        ContentValues quoteCV = new ContentValues();

                        // The price or ask is null, so this is a unknown stock!
                        // There are unknown stocks that the price is not null and ask is null,
                        // that's why we need to check both values
                        if (price == null || ask == null) {
                            quoteCV.put(Contract.Quote.COLUMN_IS_UNKNOWN, Utils.UNKNOWN_STOCK);
                        } else {
                            BigDecimal change = quote.getChange();
                            BigDecimal percentChange = quote.getChangeInPercent();

                            // WARNING! Don't request historical data for a stock that doesn't exist
                            // The request will hang forever X_x
                            List<HistoricalQuote> history = stock.getHistory(from, to, Interval
                                    .WEEKLY);

                            StringBuilder historyBuilder = new StringBuilder();

                            for (HistoricalQuote it : history) {
                                historyBuilder.append(it.getDate().getTimeInMillis());
                                historyBuilder.append(", ");
                                historyBuilder.append(it.getClose());
                                historyBuilder.append("\n");
                            }

                            quoteCV.put(Contract.Quote.COLUMN_PRICE, price.floatValue());
                            quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, change
                                    .floatValue());
                            quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, percentChange
                                    .floatValue());
                            quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                            quoteCV.put(Contract.Quote.COLUMN_IS_UNKNOWN, Utils.KNOWN_STOCK);

                        }
                        context.getContentResolver().update(Contract.Quote.uri, quoteCV,
                                Contract.Quote.COLUMN_SYMBOL + "=?", new String[]{symbol});
                    }
                }
            }
        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(Utils.ACTION_SYNC_ERROR);
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");

        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context,
                QuoteJobService.class));

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context
                .JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    synchronized public static void initialize(final Context context) {
        schedulePeriodic(context);
        syncImmediately(context);
    }

    synchronized public static void syncImmediately(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context,
                    QuoteJobService.class));

            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context
                    .JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());
        }
    }
}
