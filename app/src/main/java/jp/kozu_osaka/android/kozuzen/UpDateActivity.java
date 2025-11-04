package jp.kozu_osaka.android.kozuzen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.kozu_osaka.android.kozuzen.util.PermissionsStatus;

public final class UpDateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //ダウンロード状況に応じたUIの動的変更
        Button startDownloadButton = findViewById(R.id.button_update_startUpdate);
        startDownloadButton.setClickable(!isDownloadingUpDate());
        startDownloadButton.setOnClickListener(new OnStartDownloadingButtonClicked());
        ProgressBar progressCircle = findViewById(R.id.view_update_progressCircle);
        progressCircle.setVisibility(isDownloadingUpDate() ? View.VISIBLE : View.GONE);
        TextView downloadingText = findViewById(R.id.view_update_textView_downloadingText);
        downloadingText.setVisibility(isDownloadingUpDate() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setIsDownloadingUpdate(boolean flag) {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        pref.edit().putBoolean(SHARED_PREFERENCE_IS_PROCESSING_KEY, flag).apply();

        //UIの変更
        Button startDownloadButton = findViewById(R.id.button_update_startUpdate);
        startDownloadButton.setClickable(!isDownloadingUpDate());
        ProgressBar progressCircle = findViewById(R.id.view_update_progressCircle);
        progressCircle.setVisibility(isDownloadingUpDate() ? View.VISIBLE : View.GONE);
        TextView downloadingText = findViewById(R.id.view_update_textView_downloadingText);
        downloadingText.setVisibility(isDownloadingUpDate() ? View.VISIBLE : View.GONE);
    }

    private boolean isDownloadingUpDate() {
        SharedPreferences pref = KozuZen.getInstance().getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        if(!pref.contains(SHARED_PREFERENCE_IS_PROCESSING_KEY)) {
            setIsDownloadingUpdate(false);
        }
        return pref.getBoolean(SHARED_PREFERENCE_IS_PROCESSING_KEY, false);
    }

    private final class OnStartDownloadingButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            //権限確認
            if(!PermissionsStatus.isAllowedInstallPackage()) {
                Runnable onPositive = () -> {
                    Toast.makeText(UpDateActivity.this, R.string.toast_update_downloadPermission_onPositive, Toast.LENGTH_LONG).show();
                    downloadApk();
                };
                Runnable onNegative = () -> {
                    Toast.makeText(UpDateActivity.this, R.string.toast_update_downloadPermission_onNegative, Toast.LENGTH_LONG).show();
                };
                PermissionsStatus.createDialogInstallPackages(UpDateActivity.this, onPositive, onNegative).show();
            } else {
                downloadApk();
            }
        }
    }
}