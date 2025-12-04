package jp.kozu_osaka.android.kozuzen.tutorial.group;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.TutorialContent;

public final class CollectGroup extends TutorialFragmentGroup {

    public CollectGroup() {
        super(R.string.text_tutorial_group_title_collect,
                TutorialContent.createFromLayout(R.layout.tutorial_content_collect),
                null,
                TutorialContent.createFromLayout(R.layout.tutorial_content_collect2)
        );
    }
}
