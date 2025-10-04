package jp.kozu_osaka.android.kozuzen.access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import jp.kozu_osaka.android.kozuzen.R;

/**
 * ローディング画面を表示させるFragment
 */
public final class LoadingFragment extends Fragment {

    public static final String LOADING_FRAGMENT_TAG = "DeChain_loadingFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }
}