package jp.kozu_osaka.android.kozuzen.access.task.foreground;

import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jp.kozu_osaka.android.kozuzen.AuthorizationActivity;
import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.access.DeChainSpreadSheet;
import jp.kozu_osaka.android.kozuzen.access.argument.ResetPasswordArguments;
import jp.kozu_osaka.android.kozuzen.access.request.ResetPasswordRequest;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

/**
 * Spreadsheetにアカウントのパスワードのリセットを要求する。
 */
public class ResetPasswordTask extends ForegroundAccessTask {

    private final String mailAddress;
    private final HashedString newPassword;

    /**
     * @param activity SpreadSheet操作の際に表示させるロード画面を乗っける元画面。
     * @param fragmentFrameId
     */
    public ResetPasswordTask(FragmentActivity activity, int fragmentFrameId,
                             String mailAddress, HashedString newPassword) {
        super(activity, fragmentFrameId);
        this.mailAddress = mailAddress;
        this.newPassword = newPassword;
    }

    @Override
    @RunOnSubMethod
    @InterruptibleMethod
    public AccessResult run() throws ExecutionException, IOException {
        showLoadingFragment();

        if(!DeChainSpreadSheet.existsRegisteredMail(this.mailAddress)) {
            return AccessResult.Builder.failure(R.string.notification_message_resetPass_failure_accountNotFound);
        }
        DeChainSpreadSheet.sendRequestToQueue(new ResetPasswordRequest(
                new ResetPasswordArguments(this.mailAddress, this.newPassword)
        ));

        if(Thread.currentThread().isInterrupted()) {
            return null;
        }
        //認証コード画面に移行
        runOnUiThread(() -> {
            Intent intent = new Intent(this.activity, AuthorizationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, this.mailAddress);
            intent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, SixNumberCode.CodeType.FOR_PASSWORD_RESET);
            this.activity.startActivity(intent);
        });
        return AccessResult.Builder.success();
    }

    @Override
    public String getSuccessNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_resetPass_success);
    }

    @Override
    public String getFailureNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_resetPass_failure);
    }
}
