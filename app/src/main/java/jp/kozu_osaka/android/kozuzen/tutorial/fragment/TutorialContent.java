package jp.kozu_osaka.android.kozuzen.tutorial.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;

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
