package jp.kozu_osaka.android.kozuzen.tutorial.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import jp.kozu_osaka.android.kozuzen.R;

public final class EndFrameFragment extends FrameFragment {

    public EndFrameFragment(TutorialContent content) {
        super(R.layout.fragment_end_form, R.id.layout_endFragment_content, content);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button closeButton = view.findViewById(R.id.view_endFragment_close);
        closeButton.setOnClickListener(new OnCloseButtonClicked());
    }

    private final class OnCloseButtonClicked implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            if(getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}