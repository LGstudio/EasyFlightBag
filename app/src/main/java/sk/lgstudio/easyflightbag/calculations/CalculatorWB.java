package sk.lgstudio.easyflightbag.calculations;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.File;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.AirplaneEditorDialog;
import sk.lgstudio.easyflightbag.dialogs.AirplaneSelectorDialog;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;

/**
 * Created by LGstudio on 2017-01-31.
 */

public class CalculatorWB extends Calculator implements View.OnClickListener, DialogInterface.OnCancelListener {

    private File selectedPlane = null;
    private File parentFolder = null;
    private AirplaneSelectorDialog dialogAirplane;
    private AirplaneManager airplane;
    private boolean selectorDialogOpened = false;

    private ImageButton airplaneSelector = null;
    private TextView airplaneId = null;
    private ImageButton airplaneEditor = null;

    private SharedPreferences prefs;
    private Context context;

    public CalculatorWB(SharedPreferences p, Context c, File f, float[] r, float[] v, int[] l) {
        super(r, v, l);
        prefs = p;
        context = c;
        parentFolder = f;
    }

    @Override
    public void initView(View v) {

        String folder = prefs.getString(context.getString(R.string.pref_wb_selected), null);
        if (folder != null)
            selectedPlane = new File (folder);
        else

        airplaneEditor = null;
        airplaneSelector = null;
        airplaneId = null;

        airplaneSelector = (ImageButton) v.findViewById(R.id.wb_plane);
        airplaneEditor = (ImageButton) v.findViewById(R.id.wb_edit);
        airplaneId = (TextView) v.findViewById(R.id.wb_airplane_name);
        airplaneSelector.setOnClickListener(this);
        airplaneEditor.setOnClickListener(this);

        reloadContent();

    }

    @Override
    public void detachView() {// empty
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wb_plane:
                createAirplaneSelectorDialog();
                break;
            case R.id.wb_edit:
                createAirplaneEditorDialog();
                break;
        }
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneSelectorDialog(){
        selectorDialogOpened = true;
        dialogAirplane = new AirplaneSelectorDialog(context);
        dialogAirplane.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAirplane.setContentView(R.layout.dialog_airplane_selector);
        dialogAirplane.loadContent(parentFolder, true);
        dialogAirplane.selected = selectedPlane;
        dialogAirplane.setOnCancelListener(this);
        dialogAirplane.show();
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneEditorDialog(){
        selectorDialogOpened = false;
        AirplaneEditorDialog dialog = new AirplaneEditorDialog(context, R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_airplane_editor);
        dialog.loadContent(airplane);
        dialog.setOnCancelListener(this);
        dialog.show();

    }

    @Override
    public void onCancel(DialogInterface dialog) {

        if (selectorDialogOpened){
            selectedPlane = dialogAirplane.selected;
            if (selectedPlane != null){
                prefs.edit().putString(context.getString(R.string.pref_wb_selected), selectedPlane.getPath()).apply();
                airplaneEditor.setClickable(true);
            }
            else {
                prefs.edit().remove(context.getString(R.string.pref_wb_selected)).apply();
                airplaneEditor.setClickable(false);
            }
            reloadContent();
        }
        else{

        }
    }

    private void reloadContent(){
        if (selectedPlane != null){

            airplaneEditor.setVisibility(View.VISIBLE);
            airplane = new AirplaneManager(selectedPlane);
            airplaneId.setText(airplane.getName());

            if (airplane.loaded){
                //Log.w("Airplane", "created");
                // TODO: create new view matching airplane description
            }
            else {
                // TODO: clear/hide other views
            }
        }
        else {
            airplaneId.setText("");
            airplaneEditor.setVisibility(View.GONE);
            // TODO: clear/hide other views
        }
    }
}
