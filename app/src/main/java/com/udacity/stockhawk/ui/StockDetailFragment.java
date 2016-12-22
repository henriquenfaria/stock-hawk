package com.udacity.stockhawk.ui;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.udacity.stockhawk.utils.Constants;
import com.udacity.stockhawk.utils.Utils;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class StockDetailFragment extends Fragment {

    private static final String ARG_STOCK_SYMBOL = "arg_stock_symbol";
    private static final String ARG_STOCK_HISTORY = "arg_stock_history";
    private String mStockSymbol;
    private String mStockHistory;
    private OnStockDetailFragmentListener mOnStockDetailFragmentListener;
    private Context mContext;

    @BindView(R.id.chart_header)
    TextView mChartHeader;

    @BindView(R.id.stock_chart)
    LineChart mLineChart;

    @BindView(R.id.empty_detail_text)
    TextView mEmptyDetailText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StockDetailFragment() {
    }

    // Create new Fragment instance
    public static StockDetailFragment newInstance(String stockSymbol, String stockHistory) {
        StockDetailFragment fragment = new StockDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STOCK_SYMBOL, stockSymbol);
        args.putString(ARG_STOCK_HISTORY, stockHistory);
        fragment.setArguments(args);
        return fragment;
    }

    public static StockDetailFragment newInstance() {
        StockDetailFragment fragment = new StockDetailFragment();
        return fragment;
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
            mStockHistory = getArguments().getString(ARG_STOCK_HISTORY);
        }
        getActivity().setTitle(mStockSymbol);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.stock_detail_fragment, container, false);
        ButterKnife.bind(this, view);

        if (mStockSymbol == null && mStockHistory == null) {
            mEmptyDetailText.setVisibility(View.VISIBLE);
            mChartHeader.setVisibility(View.GONE);
            mLineChart.setVisibility(View.GONE);
            return view;
        }


        mChartHeader.setText(getString(R.string.chart_detail_title, mStockSymbol));
        try {
            List<Entry> entries = Utils.createEntryListFromString(mStockHistory);
            generateStockChart(entries);
        } catch (NumberFormatException exception) {
            Timber.e(exception, "Error while generating stock chart");
            mLineChart.setNoDataText(getString(R.string.error_stock_history));
        }

        return view;
    }

    private void generateStockChart(List<Entry> entries) {
        mLineChart.setNoDataText(getString(R.string.status_loading_chart_data));
        Collections.sort(entries, new EntryXComparator());
        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setForm(Legend.LegendForm.NONE);
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
        yLeftAxis.setGranularity(1);
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
        xAxis.setLabelCount(5, true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String formattedDate = Utils.formatMillisecondsForLocale((long) value);
                return formattedDate;
            }
        });

        //TODO: Enable pinch zoom?
        mLineChart.setScaleEnabled(false);

        //TODO: Need to test on other resolutions
        mLineChart.setExtraOffsets(5f, 0, 30f, 0);

        mLineChart.setData(lineData);
        mLineChart.setDrawMarkers(true);
        mLineChart.setMarker(new StockMarkerView(mContext, R.layout.stock_marker_layout));
        mLineChart.setDescription(null);
        mLineChart.animateX(Constants.Chart.CHART_X_ANIMATION_TIME, Easing.EasingOption
                .EaseInOutBack);
    }

    public interface OnStockDetailFragmentListener {
    }

}
