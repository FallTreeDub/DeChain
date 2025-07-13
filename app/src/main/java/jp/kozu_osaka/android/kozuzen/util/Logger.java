package jp.kozu_osaka.android.kozuzen.util;


import android.util.Log;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class Logger {

    private static final String TITLE_INFO = "DeChain-Info";
    private static final String TITLE_ERROR = "DeChain-Error";
    private static final String TITLE_WARN = "DeChain-Warn";

    private Logger() {}

    public static void i(@NotNull Object obj) {
        Log.i(TITLE_INFO, String.valueOf(obj));
    }

    public static void e(@NotNull Object obj) {
        Log.e(TITLE_ERROR, String.valueOf(obj));
    }

    public static void w(@NotNull Object obj) {
        Log.w(TITLE_WARN, String.valueOf(obj));
    }
}
