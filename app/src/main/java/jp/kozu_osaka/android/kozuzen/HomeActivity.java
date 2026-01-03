package jp.kozu_osaka.android.kozuzen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * ホーム画面。
 */
public final class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_home_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppCompatImageButton codeButton = findViewById(R.id.button_home_code);
        AppCompatImageButton accountButton = findViewById(R.id.button_home_account);

        codeButton.setOnClickListener(new OnCodeButtonClicked());
        accountButton.setOnClickListener(new OnAccountButtonClicked());
    }

    private final class OnCodeButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HomeActivity.this, CodeActivity.class);
            HomeActivity.this.startActivity(intent);
        }
    }

    private final class OnAccountButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
            HomeActivity.this.startActivity(intent);
        }
    }
}