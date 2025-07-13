package jp.kozu_osaka.android.kozuzen.access.task.foreground;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jp.kozu_osaka.android.kozuzen.HomeActivity;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.LoginActivity;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.access.DeChainSpreadSheet;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccount;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

/**
 * 本登録シートに特定のアカウントが登録されているかを調べるためのtask。
 */
public final class InquiryTask extends ForegroundAccessTask {

    private final String findingMail;
    private final HashedString findingPass;

    /**
     * @param findingMail 捜索する本登録アカウントのメールアドレス。
     * @param registeredPass 捜索する本登録アカウントのパスワード。
     */
    public InquiryTask(FragmentActivity activity, @IdRes int fragmentFrameId, String findingMail, HashedString registeredPass) {
        super(activity, fragmentFrameId);
        this.findingMail = findingMail;
        this.findingPass = registeredPass;
    }

    /**
     * 本登録アカウント格納シートに対して、特定のアカウントが存在するかを問い合わせる。
     * @return {@inheritDoc}
     * @throws ExecutionException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    @RunOnSubMethod
    @InterruptibleMethod
    public AccessResult run() throws ExecutionException, IOException {
        showLoadingFragment();

        if(DeChainSpreadSheet.existsRegisteredAccount(this.findingMail, this.findingPass)) {
            if(Thread.currentThread().isInterrupted()) {
                return null;
            }
            InternalRegisteredAccount.register(this.findingMail, this.findingPass);
            return AccessResult.Builder.success();
        } else {
            if(Thread.currentThread().isInterrupted()) {
                return null;
            }
            InternalRegisteredAccount.remove();
            return AccessResult.Builder.failure(getFailureNotificationMessage());
        }
    }

    @Override
    public void whenTimeOut() {
        super.whenTimeOut();
        runOnUiThread(() -> { //login画面に戻る
            Toast.makeText(this.activity, this.activity.getString(R.string.toast_timeout), Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(this.activity, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(loginIntent);
        });
    }

    @Override
    public void whenFailed(AccessResult.Failure result) {
        super.whenFailed(result);
        runOnUiThread(() -> {
            Toast.makeText(this.activity, R.string.toast_inquiry_notFound, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this.activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(intent);
        });
    }

    @Override
    public void whenSuccess() {
        super.whenSuccess();
        runOnUiThread(() -> {
            Intent intent = new Intent(this.activity, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.activity.startActivity(intent);
        });
    }

    @Override
    public String getSuccessNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_inquiry_success);
    }

    @Override
    public String getFailureNotificationMessage() {
        return KozuZen.getInstance().getString(R.string.notification_message_inquiry_failure);
    }
}
