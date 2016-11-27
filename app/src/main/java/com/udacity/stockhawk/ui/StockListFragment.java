package com.udacity.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.listener.OnStockListFragmentListener;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.udacity.stockhawk.R.id.error;
import static com.udacity.stockhawk.R.id.fab;

public class StockListFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(fab)
    FloatingActionButton mAddButton;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(error)
    TextView mErrorTextView;

    private StockAdapter adapter;

    private final SyncErrorReceiver mReceiver = new SyncErrorReceiver();


    // TODO: Use ButterKnife?
    private Context mContext;

    private OnStockListFragmentListener mOnStockListFragmentListener;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StockListFragment() {
    }


    // Create new Fragment instance
    public static StockListFragment newInstance() {
        StockListFragment fragment = new StockListFragment();
        return fragment;
    }

    @Override
    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnStockListFragmentListener) {
            mOnStockListFragmentListener = (OnStockListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStockListFragmentListener");
        }

        mContext = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(mContext)
                    .registerReceiver(mReceiver, new IntentFilter(Utils.ACTION_SYNC_ERROR));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stock_list_fragment, container, false);

        //TODO: Is it ok? What about mContext?
        ButterKnife.bind(this, view);

        firstRunCheck();

        adapter = new StockAdapter(mContext, this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(mContext);
        //TODO: What about using mContext somehow?
        getActivity().getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                //TODO: What about using mContext somehow?
                getActivity().getContentResolver().delete(Contract.Quote.makeUriForStock(symbol),
                        null, null);
            }
        }).attachToRecyclerView(mRecyclerView);


        mAddButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO: What about using mContext somehow?
                AddStockDialog addStockDialog = new AddStockDialog();

                // TODO: Create CODE and TAG in Constants
                addStockDialog.setTargetFragment(StockListFragment.this, 1);
                addStockDialog.show(getActivity().getSupportFragmentManager(),
                        "StockDialogFragment");
            }
        });

        return view;
    }


    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(mContext);

        if (!networkUp() && adapter.getItemCount() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mErrorTextView.setText(getString(R.string.error_no_network));
            mErrorTextView.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(mContext, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (adapter.getItemCount() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mErrorTextView.setText(getString(R.string.error_no_stocks));
            mErrorTextView.setVisibility(View.VISIBLE);
        } else {
            mErrorTextView.setVisibility(View.GONE);
        }
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                mSwipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            }

            ContentValues quoteCV = new ContentValues();
            quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
            mContext.getContentResolver().insert(Contract.Quote.uri, quoteCV);
            QuoteSyncJob.syncImmediately(mContext);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //TODO: Constants
        if (requestCode == 1 && resultCode == 1 && data != null && data.hasExtra("stockName")) {
            addStock(data.getStringExtra("stockName"));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext,
                Contract.Quote.uri,
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (data != null && data.getCount() != 0) {
            mErrorTextView.setVisibility(View.GONE);
        }
        adapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (Utils.getDisplayMode(mContext)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stock_list_fragment_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_item_change_units);
        setDisplayModeMenuItemIcon(item);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_item_change_units) {
            Utils.toggleDisplayMode(mContext);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Code based on
    // http://stackoverflow.com/questions/7217578/check-if-application-is-on-its-first-run
    private void firstRunCheck() {

        final int DOESNT_EXIST = -1;

        // Get current version code
        final int currentVersionCode;

        try {
            currentVersionCode = mContext.getPackageManager().getPackageInfo(mContext
                    .getPackageName(), 0)
                    .versionCode;
        } catch (PackageManager.NameNotFoundException exception) {
            Timber.e(exception, "Error getting current version code");
            return;
        }

        // Get saved version code
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int savedVersionCode = prefs.getInt(getResources().getString(R.string.pref_version_code),
                DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            //Normal run. Nothing to do here.
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {
            String[] defaultStocks = getResources().getStringArray(R.array.default_stocks);
            for (int i = 0; i < defaultStocks.length; i++) {
                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, defaultStocks[i]);
                mContext.getContentResolver().insert(Contract.Quote.uri, quoteCV);
            }

        } else if (currentVersionCode > savedVersionCode) {
            //This is an app upgrade. Nothing to do here (yet).
            return;
        }

        prefs.edit().putInt(getString(R.string.pref_version_code), currentVersionCode).commit();
    }


    private class SyncErrorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                //TODO: Call this method from the Fragment?
                //swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, R.string.toast_sync_error_try_again, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

}
