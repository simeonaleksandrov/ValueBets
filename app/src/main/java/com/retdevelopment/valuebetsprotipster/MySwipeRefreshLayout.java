package com.retdevelopment.valuebetsprotipster;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Svetoslav on 28.3.2018 г..
 */

public class MySwipeRefreshLayout extends SwipeRefreshLayout
{
    private boolean paused;

    public MySwipeRefreshLayout(Context context)
    {
        super(context);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (paused)
        {
            return false;
        } else
        {
            return super.onInterceptTouchEvent(ev);
        }
    }

    public void setPaused(boolean paused)
    {
        this.paused = paused;
    }
}
