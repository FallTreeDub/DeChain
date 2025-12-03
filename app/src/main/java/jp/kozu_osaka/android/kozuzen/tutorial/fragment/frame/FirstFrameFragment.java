package jp.kozu_osaka.android.kozuzen.tutorial.fragment.frame;

import android.os.Bundle;

import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.content.ContentFragment;

public final class FirstFrameFragment extends FrameFragment {

    public FirstFrameFragment(ContentFragment content) {
        super(R.layout.fragment_first_form, R.id.layout_firstFragment_content, content);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}