package jp.kozu_osaka.android.kozuzen.tutorial.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import jp.kozu_osaka.android.kozuzen.R;

/**
 * {@link TutorialContent}を表示チュートリアルコンテンツとしてはめ込むことができるFragment。
 */
public class FrameFragment extends Fragment {

    @LayoutRes
    private final int layoutIDOfThis;
    @IdRes
    private final int frameIDForContent;
    private final TutorialContent content;

    /**
     *
     * @param layoutIDOfThis この{@link FrameFragment}のlayoutID。
     * @param frameForContent {@code content}をはめ込むための{@link FrameLayout}のID。
     * @param content チュートリアルコンテンツが載っているContentFragment。
     */
    protected FrameFragment(@LayoutRes int layoutIDOfThis, @IdRes int frameForContent, TutorialContent content) {
        this.layoutIDOfThis = layoutIDOfThis;
        this.frameIDForContent = frameForContent;
        this.content = content;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(this.layoutIDOfThis, container, false);
    }

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View frameView = view.findViewById(this.frameIDForContent);
        if(frameView instanceof FrameLayout) {
            View contentView = getLayoutInflater().inflate(this.content.getLayoutID(), (FrameLayout)frameView, false);
            ((FrameLayout)frameView).addView(contentView);
        }
    }
}
