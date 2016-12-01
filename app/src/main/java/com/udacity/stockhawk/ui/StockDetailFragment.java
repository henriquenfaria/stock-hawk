package com.udacity.stockhawk.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailFragment extends Fragment {

    private static final String ARG_STOCK_SYMBOL = "arg_stock_symbol";
    private String mStockSymbol;
    private OnStockDetailFragmentListener mOnStockDetailFragmentListener;
    private Context mContext;

    @BindView(R.id.stock_symbol)
    TextView mStockSymbolTextView;

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
            getActivity().setTitle(mStockSymbol);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stock_detail_fragment, container, false);
        ButterKnife.bind(this, view);
        mStockSymbolTextView.setText(mStockSymbol);
        return view;
    }

    public interface OnStockDetailFragmentListener {
    }
}
