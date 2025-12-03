package jp.kozu_osaka.android.kozuzen.tutorial.fragment.frame;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;

import jp.kozu_osaka.android.kozuzen.tutorial.fragment.content.ContentFragment;

/**
 * {@link androidx.fragment.app.Fragment}を表示チュートリアルコンテンツとしてはめ込むことができるFragment。
 */
public class FrameFragment extends Fragment {

    @LayoutRes
    private final int layoutIDOfThis;
    @IdRes
    private final int frameIDForContent;
    private final ContentFragment content;

    /**
     *
     * @param layoutIDOfThis この{@link FrameFragment}のlayoutID。
     * @param frameForContent {@code content}をはめ込むための{@link FrameLayout}のID。
     * @param content チュートリアルコンテンツが載っているContentFragment。
     */
    public FrameFragment(@LayoutRes int layoutIDOfThis, @IdRes int frameForContent, ContentFragment content) {
        this.layoutIDOfThis = layoutIDOfThis;
        this.frameIDForContent = frameForContent;
        this.content = content;
    }

    @Override
    @CallSuper
    public void onCreate(Bundle savedInstanceStatus) {
        super.onCreate(savedInstanceStatus);
        if(getActivity() == null) {
            return;
        }
        View view = getActivity().findViewById(this.frameIDForContent);
        if(view instanceof FrameLayout) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(this.frameIDForContent, this.content)
                    .commit(); //枠にコンテンツとしてのfragmentをはめ込む
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(this.layoutIDOfThis, container, false);
    }
}
