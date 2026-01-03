package jp.kozu_osaka.android.kozuzen.net.usage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccountManager;

/**
 * pendされた{@link BroadcastReceiver}はデバイス再起動時に消去される。
 * 再起動時にAndroid OSによって停止された{@link UsageDataBroadcastReceiver}を再pendする。
 */
public final class RePendingBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == null) return;

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            if(InternalRegisteredAccountManager.isRegistered()) {
                UsageDataBroadcastReceiver.pendThis(context);
            }
        }
    }
}
