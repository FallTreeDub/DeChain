package jp.kozu_osaka.android.kozuzen.tutorial.group;

import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.TutorialContent;

public final class CreateAccountGroup extends TutorialFragmentGroup {

    public CreateAccountGroup() {
        super(R.string.text_tutorial_group_title_account,
                TutorialContent.createFromLayout(R.layout.tutorial_content_account),
                TutorialContent.createFromLayouts(
                        R.layout.tutorial_content_account2,
                        R.layout.tutorial_content_account3,
                        R.layout.tutorial_content_account4),
                TutorialContent.createFromLayout(R.layout.tutorial_content_account5)
        );
    }
}
