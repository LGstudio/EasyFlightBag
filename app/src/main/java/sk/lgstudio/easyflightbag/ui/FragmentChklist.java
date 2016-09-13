package sk.lgstudio.easyflightbag.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;

/**
 *
 */
public class FragmentChklist extends Fragment implements View.OnClickListener {

    private ImageButton check;
    private TextView createNew;
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

    protected int selectedFile = -1;
    protected int actualTask = 0;

    private File folder = new File(Environment.getExternalStorageDirectory() + "/EasyFlightBag/Checklists");


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
        View view = inflater.inflate(R.layout.fragment_chklist, container, false);

        // ---------------------------------------------------------------------------------------------
        check = (ImageButton) view.findViewById(R.id.chklist_done);
        check.setOnClickListener(this);
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
        doneAdapter = new LineAdapter(getContext(), R.layout.chk_list_content_done, tasksDone);
        listContentDone.setAdapter(doneAdapter);
        actualAdapter = new LineAdapter(getContext(), R.layout.chk_list_content_actual, tasksActual);
        listContentActual.setAdapter(actualAdapter);
        nextAdapter = new LineAdapter(getContext(), R.layout.chk_list_content_next, tasksNext);
        listContentNext.setAdapter(nextAdapter);

        // ---------------------------------------------------------------------------------------------
        fAdapter = new FileAdapter(getContext(), R.layout.chk_list_file_row, getFiles());

        View header = inflater.inflate(R.layout.chk_list_file_header, null);
        createNew = (TextView) header.findViewById(R.id.chklist_new);
        showMenu = (ImageView) header.findViewById(R.id.chklist_menu);
        createNew.setOnClickListener(this);
        listFiles.addHeaderView(header);

        listFiles.setAdapter(fAdapter);

        // ---------------------------------------------------------------------------------------------
        alignColumns();

        return view;
    }

    /**
    * ----------------------------------------------------------------------------------------------------
    *   FILE READING
    */

    public File[] getFiles(){
        return folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
    }

    /**
     * ----------------------------------------------------------------------------------------------------
     *   CLICK HANDLING
     */

    private void handleOnFileClick(int pos){ // !!! HEADER IS POSITION 0
        selectedFile = pos - 1;
        listFiles.setSelection(pos);
        fAdapter.notifyDataSetChanged();

        actualTask = 0;
        tasks.clear();

        alignColumns();

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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            loadTasks();

        }
        else {
            clearTasks();
        }
    }

    private void handleOnPreviousClick(int pos){
        actualTask = pos;
        loadTasks();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chklist_done:
                actualTask += 1;
                loadTasks();
                break;
            case R.id.chklist_new:
                //Log.e("CLICK", "NEW");
                break;
        }
    }

    /**
     * ----------------------------------------------------------------------------------------------------
     *   REFRESH SCREEN
     */

    private void alignColumns(){

        float left = 0.7f, right = 0.3f;

        if (selectedFile > -1) {
            check.setVisibility(View.VISIBLE);
            showMenu.setVisibility(View.VISIBLE);
            left = 0.35f;
            right = 0.65f;
        }
        else {
            check.setVisibility(View.INVISIBLE);
            showMenu.setVisibility(View.INVISIBLE);
        }

        LinearLayout.LayoutParams paramL = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, left);

        LinearLayout.LayoutParams paramR = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, right);

        leftScrollView.setLayoutParams(paramL);
        rightScrollView.setLayoutParams(paramR);

    }

    private void loadTasks(){
        if (tasks.size() > actualTask) {

            tasksDone.clear();
            tasksActual.clear();
            tasksNext.clear();

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

            int newHeigth = (int) ((rightScrollView.getHeight() - listContentActual.getHeight()) / 2);

            ViewGroup.LayoutParams paramsDone = listContentDone.getLayoutParams();
            paramsDone.height = newHeigth;
            listContentDone.setLayoutParams(paramsDone);

            ViewGroup.LayoutParams paramsNext = listContentNext.getLayoutParams();
            paramsNext.height = newHeigth;
            listContentNext.setLayoutParams(paramsNext);

        }
    }

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
            data = d;
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
                row.setBackgroundResource(R.drawable.bck_file_selected);
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