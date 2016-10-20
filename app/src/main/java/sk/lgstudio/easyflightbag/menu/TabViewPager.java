package sk.lgstudio.easyflightbag.menu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Gabriel Lehocky
 *
 * extends ViewPager with the possibility to disable swipe events
 */
public class TabViewPager extends android.support.v4.view.ViewPager{

    private boolean enabled;

    public TabViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}