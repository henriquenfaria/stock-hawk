package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.Constants;
import com.udacity.stockhawk.utils.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    final private Context mContext;
    final private DecimalFormat mDollarFormatWithPlus;
    final private DecimalFormat mDollarFormat;
    final private DecimalFormat mPercentageFormat;
    private Cursor mCursor;
    private StockAdapterOnClickHandler mClickHandler;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.mContext = context;
        this.mClickHandler = clickHandler;

        mDollarFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        mDollarFormat.setMinimumIntegerDigits(1);
        mDollarFormat.setMinimumFractionDigits(2);
        mDollarFormat.setPositivePrefix("$");
        mDollarFormat.setNegativePrefix("$");
        mDollarFormatWithPlus = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        mDollarFormatWithPlus.setMinimumIntegerDigits(1);
        mDollarFormatWithPlus.setMinimumFractionDigits(2);
        mDollarFormatWithPlus.setPositivePrefix("+$");
        mDollarFormatWithPlus.setNegativePrefix("-$");
        mPercentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        mPercentageFormat.setMaximumFractionDigits(2);
        mPercentageFormat.setMinimumFractionDigits(2);
        mPercentageFormat.setPositivePrefix("+");
        mPercentageFormat.setNegativePrefix("-");
    }

    void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(mContext).inflate(R.layout.list_item_quote, parent, false);
        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.symbol.setText(mCursor.getString(mCursor.getColumnIndex(Contract.Quote
                .COLUMN_SYMBOL)));

        int stockType = mCursor.getInt(mCursor.getColumnIndex(Contract.Quote.COLUMN_TYPE));
        switch (stockType) {
            case Constants.StockType.LOADING:
                holder.priceChangeLayout.setVisibility(View.GONE);
                holder.stockStatusLayout.setVisibility(View.VISIBLE);
                holder.stockStatusText.setText(R.string.status_loading);
                break;
            case Constants.StockType.UNKNOWN:
                holder.priceChangeLayout.setVisibility(View.GONE);
                holder.stockStatusLayout.setVisibility(View.VISIBLE);
                holder.stockStatusText.setText(R.string.status_unknown_stock);
                break;
            case Constants.StockType.KNOWN:
                holder.priceChangeLayout.setVisibility(View.VISIBLE);
                holder.stockStatusLayout.setVisibility(View.GONE);
                holder.price.setText(mDollarFormat.format(mCursor.getFloat(mCursor.getColumnIndex
                        (Contract.Quote.COLUMN_PRICE))));

                float rawAbsoluteChange = mCursor.getFloat(mCursor.getColumnIndex(Contract.Quote
                        .COLUMN_ABSOLUTE_CHANGE));
                float percentageChange = mCursor.getFloat(mCursor.getColumnIndex(Contract.Quote
                        .COLUMN_PERCENTAGE_CHANGE));

                if (rawAbsoluteChange > 0) {
                    holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
                } else {
                    holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
                }

                String change = mDollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = mPercentageFormat.format(percentageChange / 100);

                if (Utils.getDisplayMode(mContext)
                        .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                    holder.change.setText(change);
                } else {
                    holder.change.setText(percentage);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mCursor != null) {
            count = mCursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(String symbol, String history, int type);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.price_change_layout)
        LinearLayout priceChangeLayout;

        @BindView(R.id.stock_status_layout)
        LinearLayout stockStatusLayout;

        @BindView(R.id.stock_status_text)
        TextView stockStatusText;

        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int symbolColumn = mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            int stockTypeColumn = mCursor.getColumnIndex(Contract.Quote.COLUMN_TYPE);
            int stockHistoryColumn = mCursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
            mClickHandler.onClick(mCursor.getString(symbolColumn),
                    mCursor.getString(stockHistoryColumn), mCursor.getInt(stockTypeColumn));
        }
    }
}
