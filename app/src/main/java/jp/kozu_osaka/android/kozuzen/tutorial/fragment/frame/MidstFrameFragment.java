package jp.kozu_osaka.android.kozuzen.tutorial.fragment.frame;

import android.os.Bundle;

import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.tutorial.fragment.content.ContentFragment;

public final class MidstFrameFragment extends FrameFragment {

    public MidstFrameFragment(ContentFragment content) {
        super(R.layout.fragment_midst_form, R.id.layout_midstFragment_content, content);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}