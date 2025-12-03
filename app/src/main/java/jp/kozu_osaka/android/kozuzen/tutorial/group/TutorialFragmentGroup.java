package jp.kozu_osaka.android.kozuzen.tutorial.group;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import jp.kozu_osaka.android.kozuzen.tutorial.fragment.content.ContentFragment;

/**
 * DeChainチュートリアルの一項目。複数Fragmentを束ねる。
 */
public class TutorialFragmentGroup {

    @StringRes
    private final int tutorialGroupTitleID;

    private final ContentFragment firstContent;
    private final ContentFragment[] midstContents;
    private final ContentFragment endContent;

    protected TutorialFragmentGroup(@StringRes int groupTitleID,) {
        this.tutorialGroupTitleID = groupTitleID;
        this.childrenFragments = childrenFragments;
    }

    public final Fragment[] getChildren() {
        return this.childrenFragments;
    }

    /**
     * @return チュートリアルリストに表示する用のタイトルのID。
     */
    @StringRes
    public final int getTitleID() {
        return this.tutorialGroupTitleID;
    }
}
