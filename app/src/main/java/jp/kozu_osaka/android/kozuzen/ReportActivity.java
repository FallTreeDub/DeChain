package jp.kozu_osaka.android.kozuzen;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.internal.InternalBackgroundErrorReportManager;
import jp.kozu_osaka.android.kozuzen.security.Secrets;

/**
 * エラー発生時の報告画面。
 */
@RequireIntentExtra(extraClazz = String.class, extraKey = Constants.IntentExtraKey.REPORT_BODY)
public final class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText reportEditText = findViewById(R.id.editText_report_body);
        reportEditText.setTextIsSelectable(true);
        reportEditText.setKeyListener(null); //入力不可能にする
        reportEditText.setText(getIntent().getStringExtra(Constants.IntentExtraKey.REPORT_BODY));

        TextView reportLink = findViewById(R.id.textView_report_link);
        reportLink.setText(Secrets.BUG_REPORT_FORM_URL);

        Button copyButton = findViewById(R.id.button_report_copy);
        copyButton.setOnClickListener(new OnCopyButtonClicked());
        Button closeButton = findViewById(R.id.button_report_close);
        closeButton.setOnClickListener(new OnCloseButtonClicked());
    }

    private final class OnCloseButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            InternalBackgroundErrorReportManager.remove();
            finish();
        }
    }

    private final class OnCopyButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            EditText reportEditText = findViewById(R.id.editText_report_body);
            ClipboardManager manager = (ClipboardManager)ReportActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setPrimaryClip(ClipData.newPlainText("", reportEditText.getText().toString()));
            Toast toast = Toast.makeText(ReportActivity.this, ReportActivity.this.getString(R.string.toast_report_copy_success), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}