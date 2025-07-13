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
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.access.DeChainSpreadSheet;
import jp.kozu_osaka.android.kozuzen.access.argument.CodeRecreateArguments;
import jp.kozu_osaka.android.kozuzen.access.request.CodeRecreateRequest;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;

public final class RecreateCodeTask extends ForegroundAccessTask {

    private final String mailAddress;
    private final SixNumberCode.CodeType codeType;

    public RecreateCodeTask(FragmentActivity activity, @IdRes int fragmentFrameId,
                            String mailAddress, SixNumberCode.CodeType codeType) {
        super(activity, fragmentFrameId);
        this.mailAddress = mailAddress;
        this.codeType = codeType;
    }

    @Override
    @RunOnSubMethod
    @InterruptibleMethod
    public AccessResult run() throws ExecutionException, IOException {
        showLoadingFragment();

        DeChainSpreadSheet.sendRequestToQueue(
                new CodeRecreateRequest(this.codeType, new CodeRecreateArguments(this.mailAddress))
        );
        if(Thread.currentThread().isInterrupted()) {
            return null;
        }
        return AccessResult.Builder.success();
    }

    @Override
    public void whenTimeOut() {
        super.whenTimeOut();
        runOnUiThread(() -> {
            Toast.makeText(this.activity, R.string.toast_timeout, Toast.LENGTH_LONG).show();
            Intent authIntent = new Intent(this.activity, AuthorizationActivity.class);
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, this.mailAddress);
            authIntent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, this.codeType);
            this.activity.startActivity(authIntent);
        });
    }

    @Override
    public void whenSuccess() {
        super.whenSuccess();
        runOnUiThread(() -> {
            Toast.makeText(this.activity, KozuZen.getInstance().getString(R.string.toast_recreateCode_success), Toast.LENGTH_LONG).show();
            Intent authIntent = new Intent(this.activity, AuthorizationActivity.class);
            authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, this.mailAddress);
            authIntent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, this.codeType);
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(authIntent);
        });
    }

    @Override
    public void whenFailed(AccessResult.Failure result) {
        super.whenFailed(result);
        runOnUiThread(() -> {
            Toast.makeText(this.activity, KozuZen.getInstance().getString(R.string.toast_recreateCode_failure), Toast.LENGTH_LONG).show();
            Intent authIntent = new Intent(this.activity, AuthorizationActivity.class);
            authIntent.putExtra(Constants.IntentExtraKey.ACCOUNT_MAIL, this.mailAddress);
            authIntent.putExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE, this.codeType);
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(authIntent);
        });
    }

    @Override
    public String getSuccessNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_recreateCode_success);
    }

    @Override
    public String getFailureNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_recreateCode_failure);
    }
}
