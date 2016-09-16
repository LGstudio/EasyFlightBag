package sk.lgstudio.easyflightbag.ui;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by L on 16/09/14.
 */
public class EditorDialog extends Dialog implements View.OnClickListener {

    private ImageButton btnBack;
    private ImageButton btnImport;
    private ImageButton btnDelete;
    private ImageButton btnSave;

    private EditText textTitle;
    private EditText textList;

    private File folder;

    public EditorDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public void loadContent(File f, boolean isNew, String title, ArrayList<String> tasks){

        folder = f;

        btnBack = (ImageButton) findViewById(R.id.chk_editor_back);
        btnImport = (ImageButton) findViewById(R.id.chk_editor_import);
        btnDelete = (ImageButton) findViewById(R.id.chk_editor_delete);
        btnSave = (ImageButton) findViewById(R.id.chk_editor_save);

        btnBack.setOnClickListener(this);
        btnImport.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        if (isNew) btnDelete.setVisibility(View.GONE);
        else btnImport.setVisibility(View.GONE);

        textTitle = (EditText) findViewById(R.id.editor_edit_title);
        textList = (EditText) findViewById(R.id.editor_edit_list);

        if (!isNew && title != null){
            textTitle.setEnabled(false);
            textTitle.setText(title);
            for (String line:tasks){
                textList.setText(textList.getText() + line + "\n");
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chk_editor_back:
                dismiss();
                break;
            case R.id.chk_editor_import:
                break;
            case R.id.chk_editor_delete:
                deleteList();
                break;
            case R.id.chk_editor_save:
                saveList();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void saveList(){
        if (textTitle.getText().length() == 0){
            textTitle.setHint(getContext().getString(R.string.list_name_mandatory));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                textTitle.setHintTextColor(getContext().getColor(R.color.colorRedLight));
            else
                textTitle.setHintTextColor(getContext().getResources().getColor(R.color.colorRedLight));
        }
        else{
            try {
                File f = new File(folder, textTitle.getText() + ".txt");
                FileWriter writer = new FileWriter(f);
                writer.append(textList.getText());
                writer.flush();
                writer.close();
                Toast.makeText(getContext(), "Ckecklist saved", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dismiss();
        }
    }

    private void deleteList(){
        File f = new File(folder, textTitle.getText() + ".txt");
        if (f.delete()){
            Toast.makeText(getContext(), "Ckecklist deleted", Toast.LENGTH_SHORT).show();
            dismiss();
        }
        else {
            Toast.makeText(getContext(), "Unable to deleted checklist", Toast.LENGTH_SHORT).show();
        }
    }
}
