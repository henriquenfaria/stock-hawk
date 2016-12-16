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
        getApplicationContext().startService(nowIntent);
        return false;
    }
}
