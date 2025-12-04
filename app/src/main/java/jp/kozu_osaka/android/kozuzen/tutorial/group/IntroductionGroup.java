package jp.kozu_osaka.android.kozuzen.tutorial.group;

import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.TutorialContent;

public final class IntroductionGroup extends TutorialFragmentGroup {

    public IntroductionGroup() {
        super(R.string.text_tutorial_group_title_introduction,
                TutorialContent.createFromLayout(R.layout.tutorial_content_introduction_logos),
                TutorialContent.createFromLayouts(R.layout.tutorial_content_introduction_about, R.layout.tutorial_content_introduction_about2),
                TutorialContent.createFromLayout(R.layout.tutorial_content_introduction_about3)
        );
    }
}
