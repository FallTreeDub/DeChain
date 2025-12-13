package jp.kozu_osaka.android.kozuzen.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;
import jp.kozu_osaka.android.kozuzen.net.usage.UsageDataBroadcastReceiver;

/**
 * 再起動時にAndroid OSによって停止された{@link UsageDataBroadcastReceiver}を再起動後に再pendする。
 */
public final class RePendingBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(InternalRegisteredAccountManager.isRegistered()) {
            UsageDataBroadcastReceiver.pendThis(context);
        }
    }
}
