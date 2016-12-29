package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.utils.Constants;

public class StockListActivity extends AppCompatActivity implements StockListFragment
        .OnStockListFragmentListener, StockDetailFragment.OnStockDetailFragmentListener {

    private boolean mIsTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_list_activity);

        if (findViewById(R.id.stock_detail_fragment_container) != null) {
            mIsTwoPane = true;
        }

        if (savedInstanceState == null) {
            StockListFragment stockListFragment = StockListFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.stock_list_fragment_container, stockListFragment).commit();

            if (mIsTwoPane) {
                StockDetailFragment stockDetailFragment = StockDetailFragment
                        .newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stock_detail_fragment_container,
                                stockDetailFragment).commit();
            }
        }

        Intent intent = getIntent();
        if (intent != null && mIsTwoPane && intent.hasExtra(Constants.Extra.EXTRA_STOCK_SYMBOL)
                && intent.hasExtra(Constants.Extra.EXTRA_STOCK_HISTORY)) {
            String symbol = intent.getStringExtra(Constants.Extra.EXTRA_STOCK_SYMBOL);
            String history = intent.getStringExtra(Constants.Extra.EXTRA_STOCK_HISTORY);
            StockDetailFragment stockDetailFragment = StockDetailFragment
                    .newInstance(symbol, history);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.stock_detail_fragment_container,
                            stockDetailFragment).commit();
        }
    }

    @Override
    public void onStockListFragmentListener(String symbol, String history, int type) {
        switch (type) {
            case Constants.StockType.LOADING:
                Toast.makeText(this, R.string.loading_stock_please_wait,
                        Toast.LENGTH_LONG).show();
                break;
            case Constants.StockType.UNKNOWN:
                Toast.makeText(this, R.string.unknown_stock_no_details,
                        Toast.LENGTH_LONG).show();
                break;
            case Constants.StockType.KNOWN:
                QuoteSyncJob.stopSyncJob(this, QuoteSyncJob.JOB_TAG_ONE_OFF);
                QuoteSyncJob.stopSyncJob(this, QuoteSyncJob.JOB_TAG_PERIODIC);
                if (mIsTwoPane) {
                    StockDetailFragment stockDetailFragment = StockDetailFragment
                            .newInstance(symbol, history);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.stock_detail_fragment_container,
                                    stockDetailFragment).commit();
                } else {
                    Intent intent = new Intent(this, StockDetailActivity.class);
                    intent.putExtra(Constants.Extra.EXTRA_STOCK_SYMBOL, symbol);
                    intent.putExtra(Constants.Extra.EXTRA_STOCK_HISTORY, history);
                    startActivity(intent);
                }
                break;
        }
    }
}
