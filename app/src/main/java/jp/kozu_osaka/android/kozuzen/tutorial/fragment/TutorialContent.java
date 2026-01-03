package jp.kozu_osaka.android.kozuzen.tutorial.fragment;

import androidx.annotation.LayoutRes;

/**
 * DeChainチュートリアルの実際の表示内容。
 * 表示するLayoutのIDのみを持つ。
 */
public final class TutorialContent {

    private final int layoutID;

    private TutorialContent(@LayoutRes int layoutID) {
        this.layoutID = layoutID;
    }

    @LayoutRes
    public int getLayoutID() {
        return layoutID;
    }

    /**
     * {@code id}をメインのlayoutとした{@link TutorialContent}を作成する。
     * @param id
     * @return 作成された、{@link TutorialContent}。
     */
    public static TutorialContent createFromLayout(@LayoutRes int id) {
        return new TutorialContent(id);
    }

    public static TutorialContent[] createFromLayouts(@LayoutRes int... ids) {
        TutorialContent[] result = new TutorialContent[ids.length];
        for(int i = 0; i < ids.length; i++) {
            result[i] = createFromLayout(ids[i]);
        }
        return result;
    }
}
