package com.udacity.stockhawk.sync;


import android.content.Intent;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.udacity.stockhawk.utils.Constants;

import timber.log.Timber;

public class QuoteJobService extends JobService {

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        Timber.d("onStartJob. Job TAG: " + job.getTag());
        Intent nowIntent = new Intent(getApplicationContext(), QuoteIntentService.class);

        int jobType = job.getExtras().getInt(Constants.Extra.EXTRA_JOB_TYPE);
        nowIntent.putExtra(Constants.Extra.EXTRA_JOB_TYPE, jobType);
        if (jobType == Constants.JobType.HISTORY) {
            String symbol = job.getExtras().getString(Constants.Extra.EXTRA_STOCK_SYMBOL);
            nowIntent.putExtra(Constants.Extra.EXTRA_STOCK_SYMBOL, symbol);
        }

        getApplicationContext().startService(nowIntent);
        return false;
    }
}
