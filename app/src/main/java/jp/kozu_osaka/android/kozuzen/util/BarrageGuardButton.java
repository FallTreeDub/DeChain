package jp.kozu_osaka.android.kozuzen.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;

/**
 * 連打を防止する機構を持つ{@link Button}。
 */
public final class BarrageGuardButton extends Button {

    public BarrageGuardButton(Context context) {
        super(context);
    }

    public BarrageGuardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarrageGuardButton(Context context, AttributeSet attrs, int defStyleAttr) {
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
