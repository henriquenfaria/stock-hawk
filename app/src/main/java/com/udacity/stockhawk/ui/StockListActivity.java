package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.utils.Constants;

public class StockListActivity extends AppCompatActivity implements StockListFragment
        .OnStockListFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stock_list_activity);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            StockListFragment stockListFragment = StockListFragment.newInstance();
            fragmentTransaction.add(R.id.stock_list_fragment_container, stockListFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onStockListFragmentListener(String symbol, int type) {
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
                QuoteSyncJob.stopSyncJob(this);
                Intent intent = new Intent(this, StockDetailActivity.class).
                        putExtra(Constants.Extra.EXTRA_STOCK_SYMBOL, symbol);
                startActivity(intent);
                break;
        }
    }
}
