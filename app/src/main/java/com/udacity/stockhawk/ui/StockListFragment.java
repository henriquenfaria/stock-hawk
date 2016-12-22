package com.udacity.stockhawk.ui;

import android.app.AlertDialog;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
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
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.utils.Constants;
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

    // Loader IDs
    private static final int STOCK_LOADER = 0;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(fab)
    FloatingActionButton mAddButton;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(error)
    TextView mErrorTextView;

    private StockAdapter mAdapter;
    private final SyncEndReceiver mSyncEndReceiver = new SyncEndReceiver();
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
    public void onClick(String symbol, String history, int type) {
        Timber.d("Symbol clicked: %s Type: %d History: %s", symbol, type, history);
        mOnStockListFragmentListener.onStockListFragmentListener(symbol, history, type);
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
    public void onStart() {
        super.onStart();
        QuoteSyncJob.initializeSyncJob(mContext);
        if (!networkUp()) {
            Toast.makeText(mContext, R.string.toast_no_please_check_connectivity, Toast
                    .LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        QuoteSyncJob.stopSyncJob(mContext);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSyncEndReceiver != null) {
            mContext.registerReceiver(mSyncEndReceiver, new IntentFilter(Constants.Action
                            .ACTION_SYNC_END));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncEndReceiver != null) {
            mContext.unregisterReceiver(mSyncEndReceiver);
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

        ButterKnife.bind(this, view);

        firstRunCheck();
        mAdapter = new StockAdapter(mContext, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        onRefresh();
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
                String symbol = mAdapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                //TODO: What about using mContext somehow?
                getActivity().getContentResolver().delete(Contract.Quote.makeUriForStock(symbol),
                        null, null);
                Utils.updateWidgets(mContext);

            }
        }).attachToRecyclerView(mRecyclerView);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AddStockDialog addStockDialog = new AddStockDialog();
                addStockDialog.setTargetFragment(StockListFragment.this,
                        Constants.Dialog.STOCK_DIALOG);
                addStockDialog.show(getActivity().getSupportFragmentManager(),
                        StockListFragment.class.getSimpleName());

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

        if (!networkUp() && mAdapter.getItemCount() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mErrorTextView.setText(getString(R.string.error_no_stocks));
            mErrorTextView.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(mContext, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (mAdapter.getItemCount() == 0) {
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
            quoteCV.put(Contract.Quote.COLUMN_TYPE, Constants.StockType.LOADING);
            mContext.getContentResolver().insert(Contract.Quote.URI, quoteCV);
            QuoteSyncJob.syncImmediately(mContext);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.Dialog.STOCK_DIALOG
                && resultCode == Constants.Dialog.STOCK_DIALOG
                && data != null && data.hasExtra(Constants.Extra.EXTRA_STOCK_SYMBOL)) {
            addStock(data.getStringExtra(Constants.Extra.EXTRA_STOCK_SYMBOL));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // TODO: Remove?
        //mSwipeRefreshLayout.setRefreshing(false);

        if (data != null && data.getCount() != 0) {
            mErrorTextView.setVisibility(View.GONE);
        }
        mAdapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO: Remove?
        //mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.setCursor(null);
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
            mAdapter.notifyDataSetChanged();
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
                quoteCV.put(Contract.Quote.COLUMN_TYPE, Constants.StockType.LOADING);
                mContext.getContentResolver().insert(Contract.Quote.URI, quoteCV);
            }

        } else if (currentVersionCode > savedVersionCode) {
            //This is an app upgrade. Nothing to do here (yet).
            return;
        }

        prefs.edit().putInt(getString(R.string.pref_version_code), currentVersionCode).commit();
    }


    public interface OnStockListFragmentListener {
        void onStockListFragmentListener(String symbol, String history, int type);
    }


    public class SyncEndReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (TextUtils.equals(intent.getAction(), Constants.Action.ACTION_SYNC_END) &&
                        (intent.hasExtra(Constants.Extra.EXTRA_SYNC_RESULT_TYPE))) {

                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    int resultType = intent.getIntExtra(Constants.Extra.EXTRA_SYNC_RESULT_TYPE,
                            Constants.SyncResultType.RESULT_UNKNOWN);

                    switch (resultType) {
                        case Constants.SyncResultType.RESULT_SUCCESS:
                            // Do nothing
                            break;
                        case Constants.SyncResultType.RESULT_ERROR:
                            Toast.makeText(context, R.string.toast_sync_error_try_again, Toast
                                    .LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
}
