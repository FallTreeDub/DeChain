package jp.kozu_osaka.android.kozuzen.util;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * {@link TextWatcher}の匿名クラス作成時に不要なメソッドを追記しなくてよくするための
 * サブクラス。
 */
public class ZenTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}