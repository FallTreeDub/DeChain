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
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.access.DeChainSpreadSheet;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccount;
import jp.kozu_osaka.android.kozuzen.security.HashedString;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * 仮登録シートに特定のアカウントが登録されているかどうかを調べるためのtask。
 */
public final class TentativeInquiryTask extends ForegroundAccessTask {

    private final String findingMail;
    private final HashedString findingPass;

    public TentativeInquiryTask(FragmentActivity activity, @IdRes int fragmentFrameId, String findingMail, HashedString findingPass) {
        super(activity, fragmentFrameId);
        this.findingMail = findingMail;
        this.findingPass = findingPass;
    }

    @Override
    @RunOnSubMethod
    @InterruptibleMethod
    public AccessResult run() throws ExecutionException, IOException {
        showLoadingFragment();

        boolean exists = DeChainSpreadSheet.existsTentativeAccount(this.findingMail, this.findingPass);
        Logger.i("checked exists");
        if(Thread.currentThread().isInterrupted()) {
            return null;
        }
        if(exists) {
            Logger.i("success.");
            return AccessResult.Builder.success();
        } else {
            InternalTentativeAccount.remove();
            return AccessResult.Builder.failure(this.getFailureNotificationMessage());
        }
    }

    @Override
    public void whenTimeOut() {
        super.whenTimeOut();
        InternalTentativeAccount.remove();
        runOnUiThread(() -> {
            Toast.makeText(KozuZen.getInstance(), KozuZen.getInstance().getString(R.string.toast_timeout), Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(KozuZen.getInstance(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            KozuZen.getInstance().startActivity(loginIntent);
        });
    }

    @Override
    public void whenSuccess() {
        super.whenSuccess();
        runOnUiThread(() -> {
            Intent authIntent = new Intent(this.activity, AuthorizationActivity.class);
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, this.findingMail);
            authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_ENCRYPTED_PASSWORD, this.findingPass);
            this.activity.startActivity(authIntent);
        });
    }

    @Override
    public void whenFailed(AccessResult.Failure result) {
        super.whenFailed(result);
        runOnUiThread(() -> {
            Intent loginIntent = new Intent(this.activity, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(loginIntent);
        });
    }

    @Override
    public String getSuccessNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_tentativeInquiry_success);
    }

    @Override
    public String getFailureNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_tentativeInquiry_failure);
    }
}
