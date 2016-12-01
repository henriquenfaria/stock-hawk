package com.udacity.stockhawk.ui;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.udacity.stockhawk.R;

// Based on http://stackoverflow.com/questions/31495649
// /how-to-highlight-the-selected-value-in-mpandroid-line-chart
public class StockMarkerView extends MarkerView {

    private TextView markerTextView;
    private MPPointF mOffset;

    public StockMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        markerTextView = (TextView) findViewById(R.id.marker_content);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String formattedValue = String.format("%.2f", e.getY());
        markerTextView.setText("$" + formattedValue);
    }

    @Override
    public MPPointF getOffset() {
        if (mOffset == null) {
            // Aligning marker
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }
        return mOffset;
    }
}


