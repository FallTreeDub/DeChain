package jp.kozu_osaka.android.kozuzen.access.task.background;

import android.util.Log;

import androidx.annotation.CallSuper;

import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.access.request.Request;
import jp.kozu_osaka.android.kozuzen.access.task.AccessTask;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;
import jp.kozu_osaka.android.kozuzen.notification.NotificationProvider;

public abstract class BackgroundAccessTask extends AccessTask {
    @Override
    public final void whenSuccess() {}

    @Override
    public final void whenFailed(AccessResult.Failure failureAccessResult) {}

    /**
     * @param e 発生した例外。
     * @annotation {@link CallSuper}
     */
    @Override
    @CallSuper
    public void whenError(Exception e) {
        NotificationProvider.sendNotification(
                NotificationProvider.NotificationTitle.ON_BACKGROUND_ERROR_OCCURRED,
                R.string.notification_message_background_error
        );
        KozuZen.createErrorReport(e);
    }

    /**
     * アクセス処理終了後、UIスレッドに対して、結果に応じた画面遷移をおこなう。
     * @exception IOException Spreadsheetアクセス中に起きたIOException。
     * @annotation {@link RunOnSubMethod}
     * @annotation {@link InterruptibleMethod}
     */
    @Override
    @RunOnSubMethod
    @InterruptibleMethod
    public abstract AccessResult run() throws ExecutionException, IOException;
}
