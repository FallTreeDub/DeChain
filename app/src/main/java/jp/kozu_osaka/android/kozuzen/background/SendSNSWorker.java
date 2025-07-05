package jp.kozu_osaka.android.kozuzen.background;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccount;

public class SendSNSWorker extends Worker {

    public SendSNSWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        /*
        try {
            String internalAccountMail = InternalRegisteredAccount.Manager.getRegistered().getMailAddress();

            /*AccessThread accessThread = new AccessThread(
                    DeChainSpreadSheet.get(ServiceAccount.get()),
                    new RegisterSNSDataTask(internalAccountMail)
            );
            accessThread.start();
        } catch(Exception e) {
            KozuZen.createErrorReport(e);
            return Result.failure();
        }*/

        return Result.success();
    }
}