package jp.kozu_osaka.android.kozuzen.access.task.foreground;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jp.kozu_osaka.android.kozuzen.AuthorizationActivity;
import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.LoginActivity;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.SignupQuestion;
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.access.DeChainSpreadSheet;
import jp.kozu_osaka.android.kozuzen.access.argument.TentativeRegisterArguments;
import jp.kozu_osaka.android.kozuzen.access.request.TentativeRegisterRequest;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccount;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;
import jp.kozu_osaka.android.kozuzen.util.Logger;

public final class TentativeRegisterTask extends ForegroundAccessTask {

    private final String targetMailAddress;
    private final HashedString encryptedTargetPassword;
    private final SignupQuestion question;

    public TentativeRegisterTask(FragmentActivity activity, @IdRes int fragmentFrameId,
                                 String targetMail, HashedString encryptedPass,
                                 SignupQuestion question) {
        super(activity, fragmentFrameId);
        this.targetMailAddress = targetMail;
        this.encryptedTargetPassword = encryptedPass;
        this.question = question;
    }

    @Override
    @RunOnSubMethod
    @InterruptibleMethod
    public AccessResult run() throws ExecutionException, IOException {
        Logger.i("showing fragment");
        showLoadingFragment();

        Logger.i("check tenattive");
        if(DeChainSpreadSheet.existsTentativeAccount(this.targetMailAddress, this.encryptedTargetPassword)) {
            runOnUiThread(() -> {
                Toast.makeText(this.activity, this.activity.getString(R.string.toast_tentative_found), Toast.LENGTH_LONG).show();
            });
            return AccessResult.Builder.failure(R.string.notification_message_tentativeReg_failure_found);
        }

        Logger.i("send request");
        DeChainSpreadSheet.sendRequestToQueue(new TentativeRegisterRequest(
                new TentativeRegisterArguments(
                        this.targetMailAddress, this.encryptedTargetPassword, this.question)
        ));
        Logger.i("register internal");
        if(Thread.currentThread().isInterrupted()) {
            return null;
        }
        InternalTentativeAccount.register(this.targetMailAddress, this.encryptedTargetPassword);
        Logger.i("success");
        return AccessResult.Builder.success();
    }

    @Override
    public void whenTimeOut() {
        super.whenTimeOut();
        runOnUiThread(() -> {
            Toast.makeText(this.activity, KozuZen.getInstance().getString(R.string.toast_timeout), Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(this.activity, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(loginIntent);
        });
    }

    @Override
    public void whenSuccess() {
        super.whenSuccess();
        //次の画面へ遷移
        runOnUiThread(() -> {
            Intent authIntent = new Intent(this.activity, AuthorizationActivity.class);
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, this.targetMailAddress);
            authIntent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, SixNumberCode.CodeType.FOR_CREATE_ACCOUNT);
            this.activity.startActivity(authIntent);
        });
    }

    @Override
    public void whenFailed(AccessResult.Failure result) {
        super.whenFailed(result);
        runOnUiThread(() -> {
            Toast.makeText(this.activity, result.getMessage(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this.activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(intent);
        });
    }

    @Override
    public String getSuccessNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_tentativeReg_success);
    }

    @Override
    public String getFailureNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_tentativeReg_failure);
    }
}
