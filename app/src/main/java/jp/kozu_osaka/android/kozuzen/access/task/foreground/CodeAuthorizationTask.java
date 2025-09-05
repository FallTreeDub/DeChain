package jp.kozu_osaka.android.kozuzen.access.task.foreground;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jp.kozu_osaka.android.kozuzen.AuthorizationActivity;
import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.HomeActivity;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.LoginActivity;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.ResetPasswordActivity;
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.access.DeChainSpreadSheet;
import jp.kozu_osaka.android.kozuzen.access.argument.post.ConfirmAuthArguments;
import jp.kozu_osaka.android.kozuzen.access.request.post.ConfirmAuthRequest;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

public final class CodeAuthorizationTask extends ForegroundAccessTask {

    private final SixNumberCode enteredCode;
    private final String accountMail;

    public CodeAuthorizationTask(FragmentActivity activity, @IdRes int fragmentFrameId,
                                 SixNumberCode enteredCode, String accountMail) {
        super(activity, fragmentFrameId);
        this.accountMail = accountMail;
        this.enteredCode = enteredCode;
    }

    /**
     * コード認証を行う。
     * @return {@inheritDoc}
     * @throws ExecutionException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    @InterruptibleMethod
    public AccessResult run() throws ExecutionException, IOException {
        boolean result = DeChainSpreadSheet.isCorrectAuthCode(this.accountMail, this.enteredCode);
        if(Thread.currentThread().isInterrupted()) {
            return null;
        }
        if(result) {
            //SpreadSheetへ認証完了合図を送る
            DeChainSpreadSheet.sendRequestToQueue(new ConfirmAuthRequest(this.enteredCode.getType(), new ConfirmAuthArguments(this.accountMail)));

            if(this.enteredCode.getType().equals(SixNumberCode.CodeType.FOR_CREATE_ACCOUNT)) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(this.activity, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    this.activity.startActivity(intent);
                });
            } else if(this.enteredCode.getType().equals(SixNumberCode.CodeType.FOR_PASSWORD_RESET)) {
                runOnUiThread(() -> {
                    Toast.makeText(this.activity, R.string.toast_resetPassAuth_success, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this.activity, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    this.activity.startActivity(intent);
                });
            }
            return AccessResult.Builder.success();
        } else {
            if(this.enteredCode.getType().equals(SixNumberCode.CodeType.FOR_CREATE_ACCOUNT)) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(this.activity, AuthorizationActivity.class);
                    intent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, this.accountMail);
                    intent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, this.enteredCode.getType());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    this.activity.startActivity(intent);
                });
            } else if(this.enteredCode.getType().equals(SixNumberCode.CodeType.FOR_PASSWORD_RESET)) {
                runOnUiThread(() -> {
                    Toast.makeText(this.activity, R.string.toast_resetPassAuth_failure, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this.activity, ResetPasswordActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    this.activity.startActivity(intent);
                });
            }
            return AccessResult.Builder.failure(getFailureNotificationMessage());
        }
    }

    @Override
    public String getSuccessNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_sendcode_success);
    }

    @Override
    public String getFailureNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_sendcode_failure);
    }
}
