package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.calculations.CalculatorWB;

/**
 * Created by LGstudio on 2017-02-01.
 */

public class AirplaneEditorDialog extends Dialog implements View.OnClickListener{

    private ImageButton btnBack;
    private ImageButton btnSave;
    private TextView airplaneId;

    private File file;
    public CalculatorWB.Airplane airplane;

    public AirplaneEditorDialog(Context context) {
        super(context);
    }

    public void loadContent(CalculatorWB.Airplane ap, File f){
        file = f;
        airplane = ap;

        btnBack = (ImageButton) findViewById(R.id.airplane_editor_back);
        btnSave = (ImageButton) findViewById(R.id.airplane_editor_save);
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        airplaneId = (TextView) findViewById(R.id.airplane_editor_name);
        String name = file.getName();
        int suffix = name.lastIndexOf('.');
        airplaneId.setText(name.substring(0, suffix));



        // load content from object
        if (airplane != null){

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.airplane_editor_back:
                cancel();
                break;
            case R.id.airplane_editor_save:
                saveAirplane();
                break;
        }
    }

    private void saveAirplane(){
        //TODO: save to file & Airplane object & make Toast
    }
}
