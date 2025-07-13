package jp.kozu_osaka.android.kozuzen.access.task.foreground;

import android.content.Intent;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jp.kozu_osaka.android.kozuzen.AuthorizationActivity;
import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.LoginActivity;
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
public final class ResetPasswordTask extends ForegroundAccessTask {

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
        return AccessResult.Builder.success();
    }

    @Override
    public void whenSuccess() {
        super.whenSuccess();
        //認証コード画面に移行
        runOnUiThread(() -> {
            Intent intent = new Intent(this.activity, AuthorizationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, this.mailAddress);
            intent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, SixNumberCode.CodeType.FOR_PASSWORD_RESET);
            this.activity.startActivity(intent);
        });
    }

    @Override
    public void whenFailed(AccessResult.Failure result) {
        super.whenFailed(result);
        runOnUiThread(() -> {
            Toast.makeText(this.activity, KozuZen.getInstance().getString(R.string.toast_resetPass_failure), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this.activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(intent);
        });
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
