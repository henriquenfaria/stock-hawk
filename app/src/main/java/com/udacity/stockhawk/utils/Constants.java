package com.udacity.stockhawk.utils;

public final class Constants {


    public static class StockType {
        public static final int KNOWN = 0;
        public static final int UNKNOWN = 1;
    }

    public static class JobType {
        public static final int INVALID = -1;
        public static final int NORMAL = 0;
        public static final int HISTORY = 1;
    }

    public static class Action {
        public static final String ACTION_SYNC_ERROR = "com.udacity.stockhawk.ACTION_SYNC_ERROR";
        public static final String ACTION_HIST_SYNC_RESULT =
                "com.udacity.stockhawk.ACTION_HIST_SYNC_RESULT";
    }

    public static class Extra {
        public static final String EXTRA_STOCK_SYMBOL = "extra_stock_symbol";
        public static final String EXTRA_JOB_TYPE = "extra_job_type";
        public static final String EXTRA_HIST_QUOTE_LIST = "extra_hist_quote_list";

    }

    public static class Request {
        public static final int REQUEST_STOCK_DIALOG = 0;

    }

    public static class Result {
        public static final int RESULT_STOCK_DIALOG = 0;

    }
}

