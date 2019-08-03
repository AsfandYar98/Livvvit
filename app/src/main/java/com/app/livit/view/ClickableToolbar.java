package com.app.livit.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Rémi OLLIVIER on 09/04/2018.
 * This class is used to allow the user to click under the transparent toolbar
 * and to disallow the user to click under a colored toolbar
 */

public class ClickableToolbar extends Toolbar {
    private boolean clickable = false;

    public ClickableToolbar(Context context) {
        super(context);
    }

    public ClickableToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return clickable;
    }

    @Override
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
}
