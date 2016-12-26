package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.utils.Constants;

import timber.log.Timber;


public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");
        if (intent != null) {
            if (intent.hasExtra(Constants.Extra.EXTRA_SYNC_FROM_WIDGET)) {
                QuoteSyncJob.getQuotes(getApplicationContext(), true);

                // IntentService runs in the background thread.
                // To show a toast, we need to run it in the UI thread.
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.toast_syncing,
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                QuoteSyncJob.getQuotes(getApplicationContext(), false);
            }
        }
    }
}
