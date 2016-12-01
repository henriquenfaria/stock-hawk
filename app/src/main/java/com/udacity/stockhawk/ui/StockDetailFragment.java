package com.udacity.stockhawk.ui;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailFragment extends Fragment {

    private static final String ARG_STOCK_SYMBOL = "arg_stock_symbol";
    private String mStockSymbol;
    private OnStockDetailFragmentListener mOnStockDetailFragmentListener;
    private Context mContext;

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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stock_detail_fragment, container, false);
        ButterKnife.bind(this, view);

        generateStockChart();

        return view;
    }

    private void generateStockChart() {

        // TODO: Placeholder values for testing
        List<Entry> entries = new ArrayList();
        /*for (int i = 0; i < 20; i++) {
            entries.add(new Entry(i,i));
        }*/
        entries.add(new Entry(1998, 10));
        entries.add(new Entry(1999, 15));
        entries.add(new Entry(2000, 14));
        entries.add(new Entry(2001, 1));
        entries.add(new Entry(2002, 2));
        entries.add(new Entry(2003, 6));
        entries.add(new Entry(2004, 23));
        entries.add(new Entry(2005, 26));
        entries.add(new Entry(2006, 26));
        entries.add(new Entry(2007, 26));
        entries.add(new Entry(2008, 20));


        // Entries need to be added to a DataSet sorted by their x-position
        // https://github.com/PhilJay/MPAndroidChart/issues/2074
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
                return "$" +formattedValue;
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
        mLineChart.invalidate();
    }

    public interface OnStockDetailFragmentListener {
    }
}
