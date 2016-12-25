package com.udacity.stockhawk.widget;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.Constants;
import com.udacity.stockhawk.utils.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static com.udacity.stockhawk.R.layout.widget_layout_list_item;


public class StockDataProvider implements RemoteViewsService.RemoteViewsFactory {

    Context mContext = null;
    private Cursor mCursor = null;
    private DecimalFormat mDollarFormatWithPlus;
    private DecimalFormat mDollarFormat;
    private DecimalFormat mPercentageFormat;


    public StockDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        mDollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
        mDollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale
                .getDefault());
        mDollarFormatWithPlus.setPositivePrefix("+$");
        mPercentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        mPercentageFormat.setMaximumFractionDigits(2);
        mPercentageFormat.setMinimumFractionDigits(2);
        mPercentageFormat.setPositivePrefix("+");
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        final long identityToken = Binder.clearCallingIdentity();
        // Only KNOWN stocks should be displayed in the collection widget
        mCursor = mContext.getContentResolver().query(Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS,
                Contract.Quote.COLUMN_TYPE + " = ?",
                new String[]{"" + Constants.StockType.KNOWN},
                null);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                widget_layout_list_item);
        String symbol = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
        String history = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        views.setTextViewText(R.id.symbol, symbol);
        int stockType = mCursor.getInt(mCursor.getColumnIndex(Contract.Quote.COLUMN_TYPE));
        switch (stockType) {
            case Constants.StockType.LOADING:
                views.setViewVisibility(R.id.price_change_layout, View.GONE);
                views.setViewVisibility(R.id.stock_status_layout, View.VISIBLE);
                views.setTextViewText(R.id.stock_status_text,
                        mContext.getString(R.string.status_loading));
                views.setContentDescription(views.getLayoutId(), symbol + " "
                        + mContext.getString(R.string.status_loading));
                break;
            case Constants.StockType.UNKNOWN:
                views.setViewVisibility(R.id.price_change_layout, View.GONE);
                views.setViewVisibility(R.id.stock_status_layout, View.VISIBLE);
                views.setTextViewText(R.id.stock_status_text,
                        mContext.getString(R.string.status_unknown_stock));
                views.setContentDescription(views.getLayoutId(), symbol + " "
                        + mContext.getString(R.string.status_unknown_stock));
                break;
            case Constants.StockType.KNOWN:
                views.setViewVisibility(R.id.price_change_layout, View.VISIBLE);
                views.setViewVisibility(R.id.stock_status_layout, View.GONE);
                views.setTextViewText(R.id.price, mDollarFormat.format(mCursor.getFloat(mCursor
                        .getColumnIndex
                                (Contract.Quote.COLUMN_PRICE))));
                float rawAbsoluteChange = mCursor.getFloat(mCursor.getColumnIndex(Contract.Quote
                        .COLUMN_ABSOLUTE_CHANGE));
                float percentageChange = mCursor.getFloat(mCursor.getColumnIndex(Contract.Quote
                        .COLUMN_PERCENTAGE_CHANGE));

                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);
                }

                String change = mDollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = mPercentageFormat.format(percentageChange / 100);

                if (Utils.getDisplayMode(mContext)
                        .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                    views.setTextViewText(R.id.change, change);
                    views.setContentDescription(views.getLayoutId(), symbol + " " + change);
                } else {
                    views.setTextViewText(R.id.change, percentage);
                    views.setContentDescription(views.getLayoutId(), symbol + " " + percentage);
                }

                // Add Extra values so we can open the correct stock detail
                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(Constants.Extra.EXTRA_STOCK_SYMBOL, symbol);
                fillInIntent.putExtra(Constants.Extra.EXTRA_STOCK_HISTORY, history);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                break;
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), widget_layout_list_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getInt(mCursor.getColumnIndex(Contract.Quote._ID));
        } else {
            return position;
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
