package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.SelectorDialog;
import sk.lgstudio.easyflightbag.dialogs.ChklistEditorDialog;

/**
 *
 */
public class FragmentChklist extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {

    private final static int FILE_NONE = -1;

    private ImageButton check;
    private ImageView createNew;
    private TextView snackbar;
    private TextView airplaneType;
    private ImageView showMenu;
    private ListView listFiles;
    private ListView listContentDone;
    private ListView listContentActual;
    private ListView listContentNext;
    private LinearLayout leftScrollView;
    private LinearLayout rightScrollView;

    private FileAdapter fAdapter;
    private LineAdapter doneAdapter;
    private LineAdapter actualAdapter;
    private LineAdapter nextAdapter;

    private ArrayList<String> tasks = new ArrayList<>();
    private ArrayList<String> tasksDone = new ArrayList<>();
    private ArrayList<String> tasksActual = new ArrayList<>();
    private ArrayList<String> tasksNext = new ArrayList<>();

    private ChklistEditorDialog dialogListEdit;
    private SelectorDialog dialogAirplane;

    protected int selectedFile = FILE_NONE;
    protected int actualTask = 0;

    public File folderActual;
    public File folder;
    public SharedPreferences prefs;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            selectedFile = savedInstanceState.getInt("file");
            actualTask = savedInstanceState.getInt("task");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt("file", selectedFile);
        outState.putInt("task", actualTask);

        super.onSaveInstanceState(outState);
    }

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater layoutInflater = inflater;
        View view = layoutInflater.inflate(R.layout.fragment_chklist, container, false);

        String prefChkActual = prefs.getString(getString(R.string.pref_chk_folder), null);
        if (prefChkActual != null) {
            folderActual = new File(prefChkActual);
            if (!folderActual.exists()){
                folderActual = null;
            }
        }

        // ---------------------------------------------------------------------------------------------
        check = (ImageButton) view.findViewById(R.id.chklist_done);
        check.setOnClickListener(this);
        snackbar = (TextView) view.findViewById(R.id.chklist_snackbar);
        listFiles = (ListView) view.findViewById(R.id.chklist_list_files);
        rightScrollView = (LinearLayout) view.findViewById(R.id.chklist_scroll_right);
        leftScrollView = (LinearLayout) view.findViewById(R.id.chklist_scroll_left);
        listContentDone = (ListView) view.findViewById(R.id.chklist_list_content_done);
        listContentActual = (ListView) view.findViewById(R.id.chklist_list_content_actual);
        listContentNext = (ListView) view.findViewById(R.id.chklist_list_content_next);

        listContentNext.setEnabled(false);

        listFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleOnFileClick(position);
            }
        });

        listContentDone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleOnPreviousClick(position);
            }
        });


        // ---------------------------------------------------------------------------------------------
        doneAdapter = new LineAdapter(getContext(), R.layout.item_chk_content_faded, tasksDone);
        listContentDone.setAdapter(doneAdapter);
        actualAdapter = new LineAdapter(getContext(), R.layout.item_chk_content_actual, tasksActual);
        listContentActual.setAdapter(actualAdapter);
        nextAdapter = new LineAdapter(getContext(), R.layout.item_chk_content_faded, tasksNext);
        listContentNext.setAdapter(nextAdapter);

        View header = layoutInflater.inflate(R.layout.list_chk_file_header, null);
        airplaneType = (TextView) header.findViewById(R.id.chklist_airplane_name);
        createNew = (ImageView) header.findViewById(R.id.chklist_new);
        showMenu = (ImageView) header.findViewById(R.id.chklist_menu);
        createNew.setOnClickListener(this);
        showMenu.setOnClickListener(this);
        listFiles.addHeaderView(header);

        reloadFiles();

        return view;
    }

    /**
     * Initializes lists
     */
    private void reloadFiles(){
        fAdapter = new FileAdapter(getContext(), R.layout.item_text_bold, getFiles());
        listFiles.setAdapter(fAdapter);

        alignLayout();
        reloadTasks();
    }

    /**
    * ----------------------------------------------------------------------------------------------------
    *   FILE READING
    */

    /**
     * Gets the list of files from the checklist folder
     * @return list of files
     */
    public File[] getFiles(){
        if (folderActual != null){
            File[] files = folderActual.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt");
                }
            });

            Arrays.sort(files);

            return files;
        }

        return new File[0];
    }

    /**
     * ----------------------------------------------------------------------------------------------------
     *   CLICK HANDLING
     */

    /**
     * Handles clicks on the folders
     * @param pos
     */
    private void handleOnFileClick(int pos){ // !!! HEADER IS POSITION 0
        selectedFile = pos - 1;
        listFiles.setSelection(pos);
        fAdapter.notifyDataSetChanged();

        actualTask = 0;
        tasks.clear();

        alignLayout();

        if (selectedFile >= 0) {
            //Log.e("selected file", fAdapter.data[selectedFile].getPath());

            FileInputStream is;
            BufferedReader reader;
            final File file = new File(fAdapter.data[selectedFile].getPath());

            if (file.exists()) {
                try {
                    is = new FileInputStream(file);

                    reader = new BufferedReader(new InputStreamReader(is));
                    String line = reader.readLine();
                    while(line != null){
                        tasks.add(line);
                        line = reader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            reloadTasks();

        }
        else {
            clearTasks();
        }
    }


    /**
     * Handles click on an already done task
     * @param pos
     */
    private void handleOnPreviousClick(int pos){
        actualTask = pos;
        reloadTasks();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chklist_done:
                actualTask += 1;
                reloadTasks();
                break;
            case R.id.chklist_new:
                createEditorDialog();
                break;
            case R.id.chklist_menu:
                if (selectedFile > FILE_NONE) handleOnFileClick(0);
                else createAirplaneSelectorDialog();
                break;
        }
    }


    /**
     * ----------------------------------------------------------------------------------------------------
     *   REFRESH SCREEN
     */

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneSelectorDialog(){
        dialogAirplane = new SelectorDialog(getContext());
        dialogAirplane.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAirplane.setContentView(R.layout.dialog_selector);
        dialogAirplane.loadContent(folder, folderActual, false, SelectorDialog.TYPE_AIRPLANE);
        dialogAirplane.setOnCancelListener(this);
        dialogAirplane.setOnDismissListener(this);
        dialogAirplane.show();
    }

    /**
     * Creates Checklist editor dialog
     */
    private void createEditorDialog(){
        boolean isNew = (selectedFile < 0);

        String title = null;
        if (!isNew) {
            title = fAdapter.data[selectedFile].getName();
            int pos = title.lastIndexOf(".");
            if (pos > 0) title = title.substring(0, pos);
        }

        dialogListEdit = new ChklistEditorDialog(getContext(), R.style.FullScreenDialog);
        dialogListEdit.setContentView(R.layout.dialog_chk_editor);
        dialogListEdit.loadContent(folderActual, isNew, title, tasks);
        dialogListEdit.setOnDismissListener(this);
        dialogListEdit.show();

    }

    /**
     * Handles the both dialog onCancel events
     * @param dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        if (dialogAirplane != null) {
            if (dialogAirplane.selected == null){
                prefs.edit().remove(getString(R.string.pref_chk_folder)).apply();
                folderActual = null;
            }
            dialogAirplane = null;
        }

    }

    /**
     * Handles both dialogs onDismiss events;
     * @param dialog
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dialogAirplane != null){

            folderActual = dialogAirplane.selected;
            if (folderActual != null)
                prefs.edit().putString(getString(R.string.pref_chk_folder), folderActual.getPath()).apply();
            else
                prefs.edit().remove(getString(R.string.pref_chk_folder)).apply();

            dialogAirplane = null;
            reloadFiles();
        }
        if (dialogListEdit != null) {
            switch (dialogListEdit.returnStatus){
                case ChklistEditorDialog.SAVE_NEW:
                case ChklistEditorDialog.DELETE:
                    selectedFile = FILE_NONE;
                case ChklistEditorDialog.SAVE_EDIT:
                    actualTask = 0;
                    tasks.clear();
                    reloadFiles();
                    break;
                case ChklistEditorDialog.BACK:
                    break;
            }
            dialogListEdit = null;
        }
    }

    /**
     * Aligns the layout of the fragment
     */
    private void alignLayout(){

        float left = 0.7f, right = 0.3f;

        if (selectedFile > FILE_NONE) {
            check.setVisibility(View.VISIBLE);
            snackbar.setVisibility(View.VISIBLE);
            left = 0.35f;
            right = 0.65f;
            createNew.setImageResource(R.drawable.ic_edit);
            showMenu.setImageResource(R.drawable.ic_menu);
        }
        else {
            check.setVisibility(View.INVISIBLE);
            snackbar.setVisibility(View.INVISIBLE);
            createNew.setImageResource(R.drawable.ic_add);
            showMenu.setImageResource(R.drawable.ic_flight);
        }

        if (folderActual == null) {
            airplaneType.setText("");
            createNew.setVisibility(View.GONE);
        }
        else {
            airplaneType.setText(folderActual.getName());
            createNew.setVisibility(View.VISIBLE);
        }

        LinearLayout.LayoutParams paramL = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, left);
        LinearLayout.LayoutParams paramR = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, right);

        leftScrollView.setLayoutParams(paramL);
        rightScrollView.setLayoutParams(paramR);

    }


    /**
     * Loads tasks into the 3 separate lists in the right panel
     */
    private void reloadTasks(){
        tasksDone.clear();
        tasksActual.clear();
        tasksNext.clear();

        if (tasks.size() > actualTask) {

            int i = 0;
            while (i < actualTask) {
                tasksDone.add(tasks.get(i));
                i += 1;
            }
            if (i < tasks.size()) {
                tasksActual.add(tasks.get(i));
                i += 1;

                while (i < tasks.size()) {
                    tasksNext.add(tasks.get(i));
                    i += 1;
                }
            } else
                tasksActual.add(" ");

            doneAdapter.notifyDataSetChanged();
            actualAdapter.notifyDataSetChanged();
            nextAdapter.notifyDataSetChanged();

            snackbar.setText(String.valueOf(actualTask+1) + " / " + String.valueOf(tasks.size()));

            int newHeight =((rightScrollView.getHeight() - listContentActual.getHeight()) / 2);

            ViewGroup.LayoutParams paramsDone = listContentDone.getLayoutParams();
            paramsDone.height = newHeight;
            listContentDone.setLayoutParams(paramsDone);

            ViewGroup.LayoutParams paramsNext = listContentNext.getLayoutParams();
            paramsNext.height = newHeight;
            listContentNext.setLayoutParams(paramsNext);
        }
        else {
            selectedFile = FILE_NONE;
            alignLayout();
        }


        // TODO: Solve this bug - Only Actual task is displayed when returning to activity
        //Log.e("0", String.valueOf(rightScrollView.getHeight()));
        //Log.e("1", String.valueOf(listContentDone.getHeight()));
        //Log.e("2", String.valueOf(listContentActual.getHeight()));
        //Log.e("3", String.valueOf(listContentNext.getHeight()));
    }

    /**
     * Clears all tasks from the right panel
     */
    private void clearTasks(){
        tasksDone.clear();
        tasksActual.clear();
        tasksNext.clear();
        doneAdapter.notifyDataSetChanged();
        actualAdapter.notifyDataSetChanged();
        nextAdapter.notifyDataSetChanged();
    }


    /**
     * ----------------------------------------------------------------------------------------------------
     *   ADAPTERS
     */

    private class FileAdapter extends ArrayAdapter<File>{

        private Context context;
        private int layoutResourceId;
        public File data[] = null;

        public FileAdapter(Context c, int layoutR, File[] d){
            super(c, layoutR, d);
            context = c;
            layoutResourceId = layoutR;
            if (d != null)
                data = d;
            else
                data = new File[0];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            TextView filename = null;

            if(row == null){
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                filename = (TextView) row.findViewById(R.id.chklist_filename);
                row.setTag(filename);
            }
            else {
                filename = (TextView) row.getTag();
            }

            String name = data[position].getName();
            int pos = name.lastIndexOf(".");
            if (pos > 0) name = name.substring(0, pos);
            filename.setText(name);

            if (position == selectedFile)
                row.setBackgroundResource(R.drawable.bck_item_selected);
            else
                row.setBackgroundResource(R.drawable.bck_transparent);

            return row;
        }

    }

    private class LineAdapter extends ArrayAdapter<String>{

        private Context context;
        private int layoutResourceId;
        public ArrayList<String> data;

        public LineAdapter(Context c, int layoutR, ArrayList<String> d){
            super(c, layoutR, d);
            context = c;
            layoutResourceId = layoutR;
            data = d;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            TextView line = null;

            if(row == null){
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                line = (TextView) row.findViewById(R.id.chklist_item_text);
                row.setTag(line);
            }
            else {
                line = (TextView) row.getTag();
            }

            line.setText(data.get(position));

            return row;
        }

    }

}