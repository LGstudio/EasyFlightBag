package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by LGstudio on 2017-02-21.
 */

public class SplashDialog extends Dialog {
    public SplashDialog(Context context, int themeResId) {
        super(context, themeResId);
    }
    @Override
    public void onBackPressed(){}
}
