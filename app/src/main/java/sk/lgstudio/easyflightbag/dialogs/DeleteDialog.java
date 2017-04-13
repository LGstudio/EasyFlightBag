package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import sk.lgstudio.easyflightbag.R;

/**
 * Asks if the user is sure to delete file or folder
 *
 * LoadContent have to be called afterSetContentView and before show to init
 * - use onCancelListener to handle "yes"
 * - use onDismissListener to handle "no"
 */

public class DeleteDialog extends Dialog implements View.OnClickListener {

    public DeleteDialog(Context context) {
        super(context);
    }

    /**
     * Initializes the buttons on the view
     */
    public void loadContent(){
        Button no = (Button) findViewById(R.id.delete_btn_no);
        Button yes = (Button) findViewById(R.id.delete_btn_yes);
        no.setOnClickListener(this);
        yes.setOnClickListener(this);
    }

    /**
     * Button onClick listener handler
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.delete_btn_no:
                cancel();
                break;
            case R.id.delete_btn_yes:
                super.cancel();
                break;
        }
    }

    @Override
    public void cancel(){
        dismiss();
    }
}
