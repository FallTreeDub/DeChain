package jp.kozu_osaka.android.kozuzen.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

public final class BarrageGuardImageButton extends ImageButton {

    public BarrageGuardImageButton(@NonNull Context context) {
        super(context);
    }

    public BarrageGuardImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BarrageGuardImageButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        super.setOnClickListener((view) -> {
            view.setEnabled(false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                view.setEnabled(true);
            }, 1500L);

            listener.onClick(view);
        });
    }
}
