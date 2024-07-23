package com.simdo.g73cs.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;
import java.util.HashMap;

public class NestedScrollableHost extends FrameLayout {

    private int touchSlop;
    private float initialX;
    private float initialY;
    private HashMap<Integer,View> findViewCache;

    public NestedScrollableHost(Context context) {
        super(context);
        ViewConfiguration configuration = ViewConfiguration.get(this.getContext());
        this.touchSlop = configuration.getScaledTouchSlop();
    }

    public NestedScrollableHost(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration = ViewConfiguration.get(this.getContext());
        this.touchSlop = configuration.getScaledTouchSlop();
    }


    private ViewPager2 getParentViewPager() {
        ViewParent parent = this.getParent();
        if (!(parent instanceof View)) {
            parent = null;
        }
        View v;
        for (v = (View) parent; v != null && !(v instanceof ViewPager2); v = (View) parent) {
            parent = v.getParent();
            if (!(parent instanceof View)) {
                parent = null;
            }
        }

        View var2 = v;
        if (!(v instanceof ViewPager2)) {
            var2 = null;
        }
        return (ViewPager2) var2;
    }

    private View getChild() {
        return this.getChildCount() > 0 ? this.getChildAt(0) : null;
    }

    private boolean canChildScroll(int orientation, float delta) {
        int direction = -((int) Math.signum(delta));
        View child;
        boolean var6 = false;
        switch (orientation) {
            case 0:
                child = this.getChild();
                var6 = child != null && child.canScrollHorizontally(direction);
                break;
            case 1:
                child = this.getChild();
                var6 = child != null && child.canScrollVertically(direction);
                break;
            default:
                // throw (Throwable)(new IllegalArgumentException());
        }

        return var6;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        this.handleInterceptTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    private void handleInterceptTouchEvent(MotionEvent e) {
        ViewPager2 parentViewPager = this.getParentViewPager();
        if (parentViewPager != null) {
            int orientation = parentViewPager.getOrientation();
            if (this.canChildScroll(orientation, -1.0F) || this.canChildScroll(orientation, 1.0F)) {
                if (e.getAction() == 0) {
                    this.initialX = e.getX();
                    this.initialY = e.getY();
                    this.getParent().requestDisallowInterceptTouchEvent(true);
                } else if (e.getAction() == 2) {
                    float dx = e.getX() - this.initialX;
                    float dy = e.getY() - this.initialY;
                    boolean isVpHorizontal = orientation == 0;
                    float scaledDx = Math.abs(dx) * (isVpHorizontal ? 0.5F : 1.0F);
                    float scaledDy = Math.abs(dy) * (isVpHorizontal ? 1.0F : 0.5F);
                    if (scaledDx > (float) this.touchSlop || scaledDy > (float) this.touchSlop) {
                        if (isVpHorizontal == scaledDy > scaledDx) {
                            this.getParent().requestDisallowInterceptTouchEvent(false);
                        } else if (this.canChildScroll(orientation, isVpHorizontal ? dx : dy)) {
                            this.getParent().requestDisallowInterceptTouchEvent(true);
                        } else {
                            this.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }

            }
        }
    }



    public View findCachedViewById(int var1) {
        if (this.findViewCache == null) {
            this.findViewCache = new HashMap<>();
        }

        View var2 = (View) this.findViewCache.get(var1);
        if (var2 == null) {
            var2 = this.findViewById(var1);
            this.findViewCache.put(var1, var2);
        }

        return var2;
    }

    public void clearFindViewByIdCache() {
        if (this.findViewCache != null) {
            this.findViewCache.clear();
        }
    }
}
