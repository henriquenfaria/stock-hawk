package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteIntentService;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.StockDetailActivity;
import com.udacity.stockhawk.ui.StockListActivity;
import com.udacity.stockhawk.utils.Constants;
import com.udacity.stockhawk.utils.Utils;

public class StockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        QuoteSyncJob.schedulePeriodic(context, QuoteSyncJob.JOB_TAG_PERIODIC_WIDGET,
                QuoteSyncJob.PERIOD_SYNC_WIDGET);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        QuoteSyncJob.stopSyncJob(context, QuoteSyncJob.JOB_TAG_PERIODIC_WIDGET);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // Clicking widget title or empty layout launches StockListActivity
            Intent intent = new Intent(context, StockListActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent);
            views.setOnClickPendingIntent(R.id.widget_empty_text, pendingIntent);
            // Setting sync button intent
            Intent syncIntent = new Intent(context, QuoteIntentService.class);
            syncIntent.putExtra(Constants.Extra.EXTRA_SYNC_FROM_WIDGET, true);
            PendingIntent syncPendingIntent = PendingIntent.getService(
                    context, 0, syncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.sync, syncPendingIntent);

            views.setRemoteAdapter(R.id.widget_list,
                    new Intent(context, StockWidgetRemoteViewsService.class));

            boolean useDetailActivity = context.getResources()
                    .getBoolean(R.bool.use_detail_activity);
            Intent clickIntentTemplate = useDetailActivity
                    ? new Intent(context, StockDetailActivity.class)
                    : new Intent(context, StockListActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty_text);

            long lastUpdateMillis = Utils.getLastUpdate(context);
            if (lastUpdateMillis <= 0) {
                views.setTextViewText(R.id.last_update,
                        context.getString(R.string.last_update, "-"));
            } else {
                String formattedDate = Utils.formatMillisecondsForLocaleWithTime(lastUpdateMillis);
                views.setTextViewText(R.id.last_update,
                        context.getString(R.string.last_update, "\n" + formattedDate));
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null) {
            if (Constants.Action.ACTION_UPDATE_WIDGETS.equals(intent.getAction())) {
                ComponentName thisWidget = new ComponentName(context.getApplicationContext(),
                        StockWidgetProvider.class);
                AppWidgetManager appWidgetManager = AppWidgetManager
                        .getInstance(context.getApplicationContext());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            } else if (Constants.Action.ACTION_SYNC_END.equals(intent.getAction())) {
                if (intent.hasExtra(Constants.Extra.EXTRA_SYNC_FROM_WIDGET)) {
                    // TODO: Stop sync animation
                    if (intent.hasExtra(Constants.Extra.EXTRA_SYNC_RESULT_TYPE)) {
                        int syncResult = intent.getIntExtra(Constants.Extra.EXTRA_SYNC_RESULT_TYPE,
                                Constants.SyncResultType.RESULT_UNKNOWN);
                        switch (syncResult) {
                            case Constants.SyncResultType.RESULT_SUCCESS:
                                // Do nothing
                                break;

                            case Constants.SyncResultType.RESULT_ERROR:
                                Toast.makeText(context, R.string.toast_sync_error_try_again, Toast
                                        .LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
                // TODO: Create receiver to start sync button animation
            }
        }
    }
}