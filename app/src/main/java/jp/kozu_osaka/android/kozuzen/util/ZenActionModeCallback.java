package jp.kozu_osaka.android.kozuzen.util;

import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * {@link ActionMode.Callback}の匿名クラス作成時に不要なメソッドを追記しなくてよくするための
 * サブクラス。
 */
public class ZenActionModeCallback implements ActionMode.Callback {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) { return false; }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) { return false; }

    @Override
    public void onDestroyActionMode(ActionMode mode) {}
}
