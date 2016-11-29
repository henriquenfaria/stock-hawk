package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
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
    public void onStockListFragmentListener(String symbol) {
        Intent intent = new Intent(this, StockDetailActivity.class).
                putExtra(Constants.Extra.EXTRA_STOCK_SYMBOL, symbol);
        startActivity(intent);
    }
}
