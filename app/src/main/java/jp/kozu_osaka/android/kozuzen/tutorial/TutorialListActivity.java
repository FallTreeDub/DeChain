package jp.kozu_osaka.android.kozuzen.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.tutorial.group.TutorialFragmentGroup;
import jp.kozu_osaka.android.kozuzen.tutorial.group.TutorialFragmentGroupCategory;

public final class TutorialListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutorial_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView chooseList = findViewById(R.id.view_tutorial_choose);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TutorialFragmentGroupCategory.getGroupTitles(this));
        chooseList.setOnItemClickListener((parent, view, position, id) -> {
            TutorialFragmentGroup contentGroup = TutorialFragmentGroupCategory.fromID(position);
            if(contentGroup != null) {
                Intent intent = new Intent(TutorialListActivity.this, TutorialViewActivity.class);
                intent.putExtra(Constants.IntentExtraKey.TUTORIAL_CONTENT_ID, position);
                startActivity(intent);
            }
        });
        chooseList.setAdapter(adapter);
    }
}