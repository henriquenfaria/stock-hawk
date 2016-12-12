package com.udacity.stockhawk.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
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
    private final SyncEndReceiver mSyncEndReceiver = new SyncEndReceiver();

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
        if (mSyncEndReceiver != null) {
            LocalBroadcastManager.getInstance(mContext)
                    .registerReceiver(mSyncEndReceiver, new IntentFilter(Constants.Action
                            .ACTION_SYNC_END));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncEndReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mSyncEndReceiver);
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

        mLineChart.setNoDataText(getString(R.string.status_loading_chart_data));

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
        mLineChart.animateX(Constants.Chart.CHART_X_ANIMATION_TIME, Easing.EasingOption
                .EaseInOutBack);
    }

    public interface OnStockDetailFragmentListener {
    }

    public class SyncEndReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (TextUtils.equals(intent.getAction(), Constants.Action.ACTION_SYNC_END) &&
                        (intent.hasExtra(Constants.Extra.EXTRA_SYNC_RESULT_TYPE))) {


                    int resultType = intent.getIntExtra(Constants.Extra.EXTRA_SYNC_RESULT_TYPE,
                            Constants.SyncResultType.RESULT_UNKNOWN);

                    switch (resultType) {
                        case Constants.SyncResultType.RESULT_SUCCESS:

                            if (intent.hasExtra(Constants.Extra.EXTRA_HIST_QUOTE_LIST)) {
                                List<Entry> entries = intent.getParcelableArrayListExtra(Constants.Extra
                                        .EXTRA_HIST_QUOTE_LIST);

                                if (mLineChart != null && entries != null) {
                                    generateStockChart(entries);
                                }
                            } else {
                                Toast.makeText(context, R.string.toast_sync_error_try_again, Toast
                                        .LENGTH_LONG).show();
                            }
                            break;
                        case Constants.SyncResultType.RESULT_ERROR:
                            Toast.makeText(context, R.string.toast_sync_error_try_again, Toast
                                    .LENGTH_LONG).show();
                            break;
                        default:
                            break;

                    }
                }
            }
        }
    }
}
