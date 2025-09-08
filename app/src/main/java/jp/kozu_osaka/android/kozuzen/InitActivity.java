package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import jp.kozu_osaka.android.kozuzen.access.AccessThread;
import jp.kozu_osaka.android.kozuzen.access.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.access.task.foreground.InquiryTask;
import jp.kozu_osaka.android.kozuzen.access.task.foreground.TentativeInquiryTask;
import jp.kozu_osaka.android.kozuzen.internal.InternalBackgroundErrorReport;
import jp.kozu_osaka.android.kozuzen.internal.InternalRegisteredAccount;
import jp.kozu_osaka.android.kozuzen.internal.InternalTentativeAccount;
import jp.kozu_osaka.android.kozuzen.util.Logger;

/**
 * 内部アカウントの有無、バックグラウンド時のエラー処理を行う。
 */
public final class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_launch);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Fragment表示
        DataBaseAccessor.showLoadFragment(this, R.id.frame_loading_launch_fragmentFrame);

        //backgroundエラー確認
        String report = InternalBackgroundErrorReport.get();
        if(report != null) {
            Intent reportIntent = new Intent(this, ReportActivity.class);
            reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            reportIntent.putExtra(Constants.IntentExtraKey.REPORT_BODY, report);
            startActivity(reportIntent);
        }

        //ログイン状況確認
        InternalRegisteredAccount internalRegisteredAccount = InternalRegisteredAccount.get();
        if(internalRegisteredAccount != null) { //ログイン済みとして登録されたアカウントがある場合
            Logger.i("internal account exists.");
            
            AccessThread accessThread = new AccessThread(
                    new InquiryTask(
                            this,
                            R.id.frame_loading_launch_fragmentFrame,
                            internalRegisteredAccount.getMailAddress(),
                            internalRegisteredAccount.getEncryptedPassword())
            );
            accessThread.start();
        } else {
            //仮登録内部アカウント取得
            Logger.i("internal account does not exist.");
            InternalTentativeAccount internalTentative = InternalTentativeAccount.get();

            //仮登録内部アカウントが存在する場合
            if(internalTentative != null) {
                Logger.i("internal tentative account exists.");
                AccessThread accessThread = new AccessThread(
                        new TentativeInquiryTask(
                                this,
                                R.id.frame_loading_launch_fragmentFrame,
                                internalTentative.getMailAddress(),
                                internalTentative.getEncryptedPassword()
                        )
                );
                accessThread.start();
            } else {
                Logger.i("internal tentative account does not exist.");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FragmentManager manager =  this.getSupportFragmentManager();
        for(Fragment f : manager.getFragments()) {
            if(f.getTag() != null &&
                    f.getTag().equals(LaunchLoadingFragment.LOADING_FRAGMENT_TAG)) {
                manager.beginTransaction().remove(f).commit();
                break;
            }
        }
    }
}