package jp.kozu_osaka.android.kozuzen.tutorial;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.annotation.RequireIntentExtra;

/**
 * {@link ViewPager2}を使用しての横スライド式のチュートリアルの表示を行う。
 * どの種類のチュートリアルを表示するかはIntentExtraで決定する。
 */
@RequireIntentExtra(extraClazz = Integer.class, extraKey = Constants.IntentExtraKey.TUTORIAL_CONTENT_ID)
public final class TutorialViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutorial);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_information_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewPager2 paper = findViewById(R.id.view_information_viewPaper);
        paper.setAdapter();
    }
}