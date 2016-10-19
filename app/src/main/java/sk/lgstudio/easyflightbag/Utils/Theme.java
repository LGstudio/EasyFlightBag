package sk.lgstudio.easyflightbag.Utils;

import android.app.Activity;
import android.content.Intent;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by LGstudio on 2016-10-18.
 */

public class Theme {

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity) {
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }
    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity, boolean night) {
        if (night) {
            activity.setTheme(R.style.AppThemeDark);
        }
        else {
            activity.setTheme(R.style.AppTheme);
        }
    }
}
