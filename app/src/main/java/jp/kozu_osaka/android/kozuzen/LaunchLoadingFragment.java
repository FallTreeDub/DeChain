package jp.kozu_osaka.android.kozuzen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class LaunchLoadingFragment extends Fragment {

    public static final String LOADING_FRAGMENT_TAG = "DeChain_launchLoadingFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading_launch, container, false);
    }
}
