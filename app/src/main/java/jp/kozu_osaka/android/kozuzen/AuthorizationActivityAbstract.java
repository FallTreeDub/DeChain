package jp.kozu_osaka.android.kozuzen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;
import jp.kozu_osaka.android.kozuzen.net.DataBaseAccessor;
import jp.kozu_osaka.android.kozuzen.util.ZenTextWatcher;

/**
 * {@link ResetPassAuthActivity}と{@link CreateAccountAuthActivity}の共通した処理をまとめ上げたクラス。
 * {@link AppCompatActivity}と継承しているが、実際に画面として表示はされず、{@link ResetPassAuthActivity}や{@link CreateAccountAuthActivity}が
 * 使用される。
 */
@RequireIntentExtra(extraClazz = String.class, extraKey = Constants.IntentExtraKey.ACCOUNT_MAIL)
public abstract class AuthorizationActivityAbstract extends AppCompatActivity {

    protected String mailAddress = null;

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authorization);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(getIntent().getStringExtra(Constants.IntentExtraKey.ACCOUNT_MAIL) == null) {
            KozuZen.createErrorReport(new IllegalArgumentException("mailAddress for authActivity is null."));
            return;
        }
        mailAddress = getIntent().getStringExtra(Constants.IntentExtraKey.ACCOUNT_MAIL);

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
        Button reAuthButton = findViewById(R.id.button_authorization_enter_reSend);
        authButton.setOnClickListener(new OnAuthButtonClicked());
        reAuthButton.setOnClickListener(new OnReAuthButtonClicked());
    }

    /**
     * @return 入力された6桁コード。無効な書式の場合は{@code null}が返される。
     */
    @Nullable
    protected String getEnteredCode() {
        //コード入力確認
        LinearLayout editTextParent = findViewById(R.id.linear_authorization_editTextParent);
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
                return null;
            }
        }
        return builder.toString();
    }

    protected abstract void onAuthButtonClicked(View v);
    protected abstract void onReAuthButtonClicked(View v);

    protected class OnAuthButtonClicked implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            onAuthButtonClicked(v);
        }
    }

    protected class OnReAuthButtonClicked implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            onReAuthButtonClicked(v);
        }
    }
}