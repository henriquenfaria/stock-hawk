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
        if (intent != null && intent.hasExtra(Constants.Extra.EXTRA_JOB_TYPE)) {
            int jobType = intent.getIntExtra(Constants.Extra.EXTRA_JOB_TYPE,
                    Constants.JobType.INVALID);
            switch (jobType) {
                case Constants.JobType.NORMAL:
                    QuoteSyncJob.getQuotes(getApplicationContext());
                    break;
                case Constants.JobType.HISTORY:
                    if (intent.hasExtra(Constants.Extra.EXTRA_STOCK_SYMBOL)) {
                        String symbol = intent.getStringExtra(Constants.Extra.EXTRA_STOCK_SYMBOL);
                        QuoteSyncJob.getHistQuotes(getApplicationContext(), symbol);
                    }
                    break;
                default:
                    Timber.d("Invalid JOB_TYPE extra. Do not execute job!");
                    break;
            }


        } else {
            Timber.d("There is no JOB_TYPE extra. Do not execute job!");
        }


    }


}
