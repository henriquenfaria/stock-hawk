package com.udacity.stockhawk.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddStockDialog extends DialogFragment {

    @BindView(R.id.dialog_stock)
    EditText stock;

    private AlertDialog mDialog;
    private EditText mEditText;

    @Override
    public void onStart() {
        super.onStart();
        if (mEditText != null && mEditText.getText().length() == 0) {
            mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);

        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addStock();
                return true;
            }
        });
        builder.setView(custom);
        builder.setTitle(getString(R.string.dialog_title));
        builder.setPositiveButton(getString(R.string.dialog_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addStock();
                    }
                });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);


        // Set `EditText` to `dialog`. You can add `EditText` from `xml` too.
        mEditText = (EditText) custom.findViewById(R.id.dialog_stock);
        /*.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);*/
        //builder.setView(input);


        mDialog = builder.create();

        mEditText.addTextChangedListener(new TextWatcher() {
            private void handleText() {
                // Grab the button
                final Button okButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (mEditText.getText().length() == 0) {
                    okButton.setEnabled(false);
                } else {
                    okButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                handleText();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do
            }
        });

        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return mDialog;
    }

    private void addStock() {
        if (getTargetFragment() instanceof StockListFragment) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_STOCK_SYMBOL, stock.getText().toString());
            getTargetFragment().onActivityResult(Constants.Dialog.STOCK_DIALOG,
                    Constants.Dialog.STOCK_DIALOG, resultIntent);
        }
        dismissAllowingStateLoss();
    }
}
