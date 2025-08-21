package jp.kozu_osaka.android.kozuzen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.base.Preconditions;

import jp.kozu_osaka.android.kozuzen.access.AccessThread;
import jp.kozu_osaka.android.kozuzen.access.task.foreground.CodeAuthorizationTask;
import jp.kozu_osaka.android.kozuzen.access.task.foreground.RecreateCodeTask;
import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.security.SixNumberCode;
import jp.kozu_osaka.android.kozuzen.util.ZenTextWatcher;

/**
 * 6桁認証コードの確認画面。
 */
@RequireIntentExtra(extraClazz = String.class, extraKey = Constants.IntentExtraKey.ACCOUNT_MAIL)
@RequireIntentExtra(extraClazz = SixNumberCode.CodeType.class, extraKey = Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE)
public final class AuthorizationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authorization);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String mailAddress = Preconditions.checkNotNull(getIntent().getStringExtra(Constants.IntentExtraKey.ACCOUNT_MAIL));
        SixNumberCode.CodeType enteredCodeType = (SixNumberCode.CodeType)Preconditions.checkNotNull(getIntent().getSerializableExtra(Constants.IntentExtraKey.SIX_AUTHORIZATION_CODE_TYPE));

        //認証コード入力欄設定
        LinearLayout editTextParent = findViewById(R.id.linear_authorization_editTextParent);
        for(int i = 0; i < editTextParent.getChildCount(); i++) {
            if(!(editTextParent.getChildAt(i) instanceof EditText)) continue;
            EditText focusEditText = (EditText)editTextParent.getChildAt(i);
            focusEditText.addTextChangedListener(new ZenTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    focusEditText.clearFocus();
                    int forwardID = focusEditText.getNextFocusForwardId();
                    if(forwardID != View.NO_ID) findViewById(forwardID).requestFocus();
                }
            });
        }

        //ボタン設定
        Button authButton = findViewById(R.id.button_authorization_enter);
        authButton.setOnClickListener(v -> {
            //コード入力確認
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < editTextParent.getChildCount(); i++) {
                if(!(editTextParent.getChildAt(i) instanceof EditText)) {
                    continue;
                }
                EditText editText = (EditText)editTextParent.getChildAt(i);
                String text = editText.getText().toString();
                if(!text.isEmpty()) {
                    builder.append(text);
                } else {
                    editText.setError(getString(R.string.text_authorization_warn));
                    return;
                }
            }
            String enteredCode = builder.toString();

            try {
                AccessThread accessThread = new AccessThread(
                        new CodeAuthorizationTask(
                                this,
                                R.id.frame_authorization_fragmentFrame,
                                SixNumberCode.toInstance(enteredCode, enteredCodeType),
                                mailAddress)
                );
                accessThread.start();
            } catch(Exception e) {
                KozuZen.createErrorReport(this, e);
            }
        });

        Button reAuthButton = findViewById(R.id.button_authorization_enter_reSend);
        reAuthButton.setOnClickListener(v -> {
            AccessThread accessThread = new AccessThread(
                    new RecreateCodeTask(
                            this,
                            R.id.frame_authorization_fragmentFrame,
                            mailAddress,
                            enteredCodeType)
            );
            accessThread.start();
        });
    }
}
