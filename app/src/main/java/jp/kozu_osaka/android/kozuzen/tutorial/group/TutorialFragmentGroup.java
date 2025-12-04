package jp.kozu_osaka.android.kozuzen.tutorial.group;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import jp.kozu_osaka.android.kozuzen.tutorial.fragment.TutorialContent;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.EndFrameFragment;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.FirstFrameFragment;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.FrameFragment;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.MidstFrameFragment;

/**
 * DeChainチュートリアルの一項目。複数Fragmentを束ねる。
 */
public class TutorialFragmentGroup {

    @StringRes
    private final int tutorialGroupTitleID;

    private final TutorialContent firstContent;
    private final TutorialContent[] midstContents;
    private final TutorialContent endContent;

    protected TutorialFragmentGroup(@StringRes int groupTitleID, @NotNull TutorialContent firstContent,
                                    @Nullable TutorialContent[] midstContents, @NotNull TutorialContent endContent) {
        this.tutorialGroupTitleID = groupTitleID;
        this.firstContent = firstContent;
        this.midstContents = midstContents == null ? new TutorialContent[]{} : midstContents;
        this.endContent = endContent;
    }

    /**
     * @return チュートリアルリストに表示する用のタイトルのID。
     */
    @StringRes
    public final int getTitleID() {
        return this.tutorialGroupTitleID;
    }

    public final int getFragmentsCount() {
        return 2 + midstContents.length;
    }

    @NotNull
    public final FrameFragment[] getContents() {
        List<FrameFragment> list = new ArrayList<>();
        list.add(new FirstFrameFragment(this.firstContent));
        for(TutorialContent c : this.midstContents) {
            list.add(new MidstFrameFragment(c));
        }
        list.add(new EndFrameFragment(this.endContent));
        return list.toArray(new FrameFragment[getFragmentsCount()]);
    }
}
