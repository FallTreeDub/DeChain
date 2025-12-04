package jp.kozu_osaka.android.kozuzen.tutorial.fragment;

import android.os.Bundle;

import jp.kozu_osaka.android.kozuzen.R;

public final class MidstFrameFragment extends FrameFragment {

    public MidstFrameFragment(TutorialContent content) {
        super(R.layout.fragment_midst_form, R.id.layout_midstFragment_content, content);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}