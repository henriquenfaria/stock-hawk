package com.udacity.stockhawk.utils;

public final class Constants {


    public static class StockType {
        public static final int LOADING = 0;
        public static final int UNKNOWN = 1;
        public static final int KNOWN = 2;

    }

    public static class JobType {
        public static final int INVALID = 0;
        public static final int NORMAL = 1;
        public static final int HISTORY = 2;
    }

    public static class SyncResultType {
        public static final int RESULT_UNKNOWN = 0;
        public static final int RESULT_SUCCESS = 1;
        public static final int RESULT_ERROR = 2;
    }

    public static class Action {
        public static final String ACTION_SYNC_END = "com.udacity.stockhawk.ACTION_SYNC_END";
        public static final String ACTION_HIST_SYNC_END =
                "com.udacity.stockhawk.ACTION_HIST_SYNC_END";
    }

    public static class Extra {
        public static final String EXTRA_STOCK_SYMBOL = "extra_stock_symbol";
        public static final String EXTRA_JOB_TYPE = "extra_job_type";
        public static final String EXTRA_HIST_QUOTE_LIST = "extra_hist_quote_list";
        public static final String EXTRA_SYNC_RESULT_TYPE = "extra_sync_result_type";

    }

    public static class Dialog {
        public static final int STOCK_DIALOG = 0;
    }

    public static class Chart {
        public static final int CHART_X_ANIMATION_TIME = 2000;
    }
}

