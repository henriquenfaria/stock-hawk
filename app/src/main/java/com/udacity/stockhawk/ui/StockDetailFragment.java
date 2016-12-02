package com.udacity.stockhawk.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;

import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.utils.Constants;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class StockDetailFragment extends Fragment {

    private static final String ARG_STOCK_SYMBOL = "arg_stock_symbol";
    private String mStockSymbol;
    private OnStockDetailFragmentListener mOnStockDetailFragmentListener;
    private Context mContext;
    private final SyncErrorReceiver mSyncErrorReceiver = new SyncErrorReceiver();
    private final HistSyncReceiver mHistSyncReceiver = new HistSyncReceiver();

    @BindView(R.id.stock_chart)
    LineChart mLineChart;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StockDetailFragment() {
    }

    // Create new Fragment instance
    public static StockDetailFragment newInstance(String stockSymbol) {
        StockDetailFragment fragment = new StockDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STOCK_SYMBOL, stockSymbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSyncErrorReceiver != null) {
            LocalBroadcastManager.getInstance(mContext)
                    .registerReceiver(mSyncErrorReceiver, new IntentFilter(Constants.Action
                            .ACTION_SYNC_ERROR));
        }
        if (mHistSyncReceiver != null) {
            LocalBroadcastManager.getInstance(mContext)
                    .registerReceiver(mHistSyncReceiver, new IntentFilter(Constants.Action
                            .ACTION_HIST_SYNC_RESULT));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncErrorReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mSyncErrorReceiver);
        }
        if (mHistSyncReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mHistSyncReceiver);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        QuoteSyncJob.stopHistSyncJob(mContext);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof StockDetailFragment.OnStockDetailFragmentListener) {
            mOnStockDetailFragmentListener = (OnStockDetailFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStockDetailFragmentListener");
        }

        mContext = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mStockSymbol = getArguments().getString(ARG_STOCK_SYMBOL);
        }
        getActivity().setTitle(mStockSymbol);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stock_detail_fragment, container, false);
        ButterKnife.bind(this, view);

        QuoteSyncJob.syncHistoryImmediately(mContext, mStockSymbol);

        return view;
    }


    private void generateStockChart(List<Entry> entries) {
        Collections.sort(entries, new EntryXComparator());

        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setForm(Legend.LegendForm.NONE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //TODO: Low cubic intensity. Tweak this value and create constant.
        dataSet.setCubicIntensity(0.05f);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        //TODO: Create chart colors inside colors.xml
        dataSet.setColor(Color.GREEN);
        dataSet.setFillColor(Color.GREEN);
        dataSet.setHighlightEnabled(true);

        LineData lineData = new LineData(dataSet);
        lineData.setValueTextColor(Color.WHITE);

        // Right Y Axis styling
        YAxis yRightAxis = mLineChart.getAxisRight();
        yRightAxis.setEnabled(false);

        // Left Y Axis styling
        YAxis yLeftAxis = mLineChart.getAxisLeft();
        yLeftAxis.setTextColor(Color.WHITE);
        yLeftAxis.setAxisMinimum(0);
        yLeftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String formattedValue = String.format("%.0f", value);
                return "$" + formattedValue;
            }
        });

        // X Axis styling
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String formattedValue = String.format("%.0f", value);
                return formattedValue;
            }
        });

        mLineChart.setData(lineData);
        mLineChart.setDrawMarkers(true);
        mLineChart.setMarker(new StockMarkerView(mContext, R.layout.stock_marker_layout));
        mLineChart.setDescription(null);
        mLineChart.setNoDataText(getString(R.string.loading_chart_data));
        mLineChart.invalidate();
    }

    public interface OnStockDetailFragmentListener {
    }

    private class HistSyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(Constants.Extra.EXTRA_HIST_QUOTE_LIST)) {

                List<Entry> entries = intent.getParcelableArrayListExtra(Constants.Extra
                        .EXTRA_HIST_QUOTE_LIST);

                if (mLineChart != null && entries != null) {
                    generateStockChart(entries);
                }

            }
        }
    }

    public class SyncErrorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Toast.makeText(context, R.string.toast_sync_error_try_again, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}
