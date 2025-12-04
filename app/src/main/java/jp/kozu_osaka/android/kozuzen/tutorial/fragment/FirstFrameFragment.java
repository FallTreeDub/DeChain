package jp.kozu_osaka.android.kozuzen.tutorial.fragment;

import android.os.Bundle;

import jp.kozu_osaka.android.kozuzen.R;

public final class FirstFrameFragment extends FrameFragment {

    public FirstFrameFragment(TutorialContent content) {
        super(R.layout.fragment_first_form, R.id.layout_firstFragment_content, content);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}