package jp.kozu_osaka.android.kozuzen.tutorial;

import androidx.annotation.Nullable;

import jp.kozu_osaka.android.kozuzen.tutorial.group.IntroductionGroup;
import jp.kozu_osaka.android.kozuzen.tutorial.group.TutorialFragmentGroup;

/**
 * {@link TutorialFragmentGroup}のインスタンスをint型のIDと紐づけることで、{@link TutorialViewActivity}に
 * 引き渡せるようにする。
 */
public enum TutorialContent {
    INTROUCTION(0, new IntroductionGroup()),
    HOW_TO_JOIN(1, ),
    HOME_SCREEN(2, ),
    WHEN_ERROR(3, ),
    UPDATE(4, );

    private final int ID;

    private final TutorialFragmentGroup GROUP;

    TutorialContent(int id, TutorialFragmentGroup group) {
        this.ID = id;
        this.GROUP = group;
    }

    public int getId() {
        return this.ID;
    }

    /**
     * {@code id}と紐づいている{@link TutorialFragmentGroup}を返す。存在しない場合は{@code null}が返される。
     * @param id
     * @return
     */
    @Nullable
    public static TutorialFragmentGroup fromID(int id) {
        for(TutorialContent c : values()) {
            if(c.getId() == id) return c.GROUP;
        }
        return null;
    }
}
