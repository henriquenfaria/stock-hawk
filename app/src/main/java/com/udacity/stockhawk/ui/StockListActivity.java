package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.listener.OnStockListFragmentListener;

public class StockListActivity extends AppCompatActivity implements OnStockListFragmentListener {

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
}
