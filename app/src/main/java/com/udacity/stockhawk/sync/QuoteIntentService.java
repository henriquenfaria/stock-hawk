package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;

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
            } else {
                QuoteSyncJob.getQuotes(getApplicationContext(), false);
            }
        }
    }
}
