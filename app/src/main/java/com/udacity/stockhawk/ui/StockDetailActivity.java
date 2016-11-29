package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.utils.Constants;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;


public class StockDetailActivity extends AppCompatActivity implements StockDetailFragment
        .OnStockDetailFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_detail_activity);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Constants.Extra.EXTRA_STOCK_SYMBOL)) {
                String stockSymbol = intent
                        .getStringExtra(Constants.Extra.EXTRA_STOCK_SYMBOL);

                StockDetailFragment detailsFragment = StockDetailFragment.newInstance(stockSymbol);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.stock_detail_fragment_container, detailsFragment).commit();
            } else {
                Log.d(LOG_TAG, "Something went wrong. Intent doesn't have" +
                        " Constants.Extra.EXTRA_STOCK_SYMBOL extra. " +
                        "Finishing StockDetailActivity.");
                finish();
            }
        }
    }
}
