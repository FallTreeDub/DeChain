package jp.kozu_osaka.android.kozuzen.internal;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * バックグラウンド処理にて発生したエラーを、次のアプリ起動時に報告画面に出せるように保存されるエラーレポート。
 * SharedPreferenceに保存され、直近一回のエラーのみが保存される。
 */
public final class InternalBackgroundErrorReportManager {

    private static final String KEY_BACKGROUND_REPORT_BODY = "report_body";

    /**
     * {@code e}を原因としたエラーレポートを、SharedPreferencesに保存する。
     * @param e 発生した例外。
     */
    public static void register(Exception e) {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_BACKGROUND_REPORT, Context.MODE_PRIVATE);
        pref.edit()
                .putString(KEY_BACKGROUND_REPORT_BODY, KozuZen.generateReport(e))
                .apply();

        Logger.i("Internal worker report is registered.");
    }

    /**
     * SharedPreferencesに登録されている、バックグラウンド処理で起きたエラーを取得する。
     * SharedPreferencesはエラーが起きるごとに上書きされるため、取得されるのは直近のエラーである。
     * @return SharedPreferencesに登録されている、バックグラウンド処理で起きた直近のエラー。存在しない場合は{@code null}が返される。
     */
    @Nullable
    public static String get() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_BACKGROUND_REPORT, Context.MODE_PRIVATE);
        String readReport = pref.getString(KEY_BACKGROUND_REPORT_BODY, "");
        return readReport.isEmpty() ? null : readReport;
    }

    /**
     * 登録済みのエラーレポートを削除する。
     */
    public static void remove() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_BACKGROUND_REPORT, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }
}
