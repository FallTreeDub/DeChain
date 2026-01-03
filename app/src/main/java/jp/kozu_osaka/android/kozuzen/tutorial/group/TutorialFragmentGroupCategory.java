package jp.kozu_osaka.android.kozuzen.tutorial.group;

import android.content.Context;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import jp.kozu_osaka.android.kozuzen.tutorial.TutorialViewActivity;

/**
 * {@link TutorialFragmentGroup}をint型のIDと紐づけることで、{@link TutorialViewActivity}に
 * 引き渡せるようにする。
 */
public enum TutorialFragmentGroupCategory {
    INTROUCTION(0, new IntroductionGroup()),
    HOW_TO_CREATE_ACCOUNT(1, new CreateAccountGroup()),
    DATA_COLLECT(2, new CollectGroup()),
    WHEN_ERROR(3, new ErrorGroup()),
    UPDATE(4, new UpdateGroup());

    private final int ID;

    private final TutorialFragmentGroup GROUP;

    TutorialFragmentGroupCategory(int id, TutorialFragmentGroup group) {
        this.ID = id;
        this.GROUP = group;
    }

    public int id() {
        return this.ID;
    }

    /**
     * {@code id}と紐づいている{@link TutorialFragmentGroup}を返す。存在しない場合は{@code null}が返される。
     * @param id
     * @return 紐づいている {@link TutorialFragmentGroup}。
     */
    @Nullable
    public static TutorialFragmentGroup fromID(int id) {
        for(TutorialFragmentGroupCategory c : values()) {
            if(c.id() == id) return c.GROUP;
        }
        return null;
    }

    public static String[] getGroupTitles(@NotNull Context context) {
        String[] titles = new String[values().length];
        for(int i = 0; i < values().length; i++) {
            titles[i] = context.getString(values()[i].GROUP.getTitleID());
        }
        return titles;
    }
}
