package jp.kozu_osaka.android.kozuzen.tutorial.fragment.frame;

import android.os.Bundle;

import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.content.ContentFragment;

public final class EndFrameFragment extends FrameFragment {

    public EndFrameFragment(ContentFragment content) {
        super(R.layout.fragment_end_form, R.id.layout_endFragment_content, content);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}