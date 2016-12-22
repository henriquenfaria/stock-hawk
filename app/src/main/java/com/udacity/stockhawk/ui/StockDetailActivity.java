package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.utils.Constants;

import timber.log.Timber;

public class StockDetailActivity extends AppCompatActivity implements StockDetailFragment
        .OnStockDetailFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_detail_activity);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                String stockSymbol = null;
                String stockHistory = null;
                if (intent.hasExtra(Constants.Extra.EXTRA_STOCK_SYMBOL)) {
                    stockSymbol = intent
                            .getStringExtra(Constants.Extra.EXTRA_STOCK_SYMBOL);
                }
                if (intent.hasExtra(Constants.Extra.EXTRA_STOCK_HISTORY)) {
                    stockHistory = intent.getStringExtra(Constants.Extra.EXTRA_STOCK_HISTORY);
                }

                StockDetailFragment detailsFragment = StockDetailFragment
                        .newInstance(stockSymbol, stockHistory);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.stock_detail_fragment_container, detailsFragment).commit();
            } else {
                Timber.d("Something went wrong. Intent doesn't have extras!" +
                        "Finishing StockDetailActivity.");
                finish();
            }
        }
    }
}
