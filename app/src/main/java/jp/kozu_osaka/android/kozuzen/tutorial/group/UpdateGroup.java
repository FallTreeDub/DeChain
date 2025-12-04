package jp.kozu_osaka.android.kozuzen.tutorial.group;

import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.TutorialContent;

public final class UpdateGroup extends TutorialFragmentGroup {

    public UpdateGroup() {
        super(R.string.text_tutorial_group_title_update,
                TutorialContent.createFromLayout(R.layout.tutorial_content_update),
                null,
                TutorialContent.createFromLayout(R.layout.tutorial_content_update2)
        );
    }
}
