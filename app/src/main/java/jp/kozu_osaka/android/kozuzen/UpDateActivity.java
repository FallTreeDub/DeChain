package jp.kozu_osaka.android.kozuzen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.kozu_osaka.android.kozuzen.net.update.DeChainUpDater;

/**
 * {@link LoginActivity}のログイン画面左下の「アップデートを確認」ボタンよりアクセス可能な、アップデートを開始するための画面。
 */
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
        SharedPreferences pref = this.getSharedPreferences(Constants.SharedPreferences.PATH_UPDATE_PROCESS_STATUS, Context.MODE_PRIVATE);
        pref.registerOnSharedPreferenceChangeListener(new OnUpdateStatusChangedListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadUI();
    }

    private void reloadUI() {
        //ダウンロード状況に応じたUIの動的変更
        Button startDownloadButton = findViewById(R.id.button_update_startUpdate);
        startDownloadButton.setEnabled(!DeChainUpDater.isRunning(this));
        startDownloadButton.setOnClickListener(new OnStartDownloadingButtonClicked());
        ProgressBar progressCircle = findViewById(R.id.view_update_progressCircle);
        progressCircle.setVisibility(DeChainUpDater.isRunning(this) ? View.VISIBLE : View.GONE);
        TextView downloadingText = findViewById(R.id.view_update_textView_downloadingText);
        downloadingText.setVisibility(DeChainUpDater.isRunning(this) ? View.VISIBLE : View.GONE);
    }

    private final class OnStartDownloadingButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                DeChainUpDater.enqueueUpdate(UpDateActivity.this);
            } catch(IllegalStateException e) {
                KozuZen.createErrorReport(e);
            }
        }
    }

    private final class OnUpdateStatusChangedListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
            if(key != null && key.equals(DeChainUpDater.SHARED_PREFERENCE_KEY_STATUS)) {
                reloadUI();
            }
        }
    }
}