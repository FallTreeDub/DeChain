package jp.kozu_osaka.android.kozuzen.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.StringRes;

/**
 * 簡素に{@link android.app.Dialog}を作成するためのクラス。
 */
public final class DialogProvider {

    private DialogProvider() {}

    /**
     * ダイアログとして表示するための必要最低限の機能を備えたダイアログを生成する。
     * {@link DialogInterface.OnClickListener}などを追加したい際は、戻り値の{@link AlertDialog}
     * 上のメソッドで追加する。
     * @param context
     * @param title
     * @param msg
     * @return
     */
    public static AlertDialog.Builder makeBuilder(Context context, @StringRes int title, @StringRes int msg) {
        return new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(msg);
    }
}
