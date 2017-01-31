package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.PdfViewerDialog;

/**
 *
 */
public class FragmentAip extends Fragment {

    private LinearLayout scrollLayout;
    private HorizontalScrollView horizontalScrollView;

    public File folder;

    public ArrayList<FileStructure> files = new ArrayList<>();
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    private ArrayList<FileAdapter> adapters = new ArrayList<>();
    private int openedLevel = 1;

    private int screenWidth;

    LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup container, Bundle savedInstanceState) {
        inflater = i;
        View view = inflater.inflate(R.layout.fragment_aip, container, false);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;

        horizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.aip_list_scroll);
        scrollLayout = (LinearLayout) view.findViewById(R.id.aip_list_layout);

        //if (fillData) { TODO: FIX THIS
        files.clear();
        getData();
        //}
        fillView();



        return view;
    }


    /**
     * Loads data from data file to the files ArrayList
     */
    private void getData(){
        FileInputStream is;
        BufferedReader reader;
        final File data = new File(folder.getPath() + getString(R.string.file_aip_data));

        int lastLevel = 0;
        ArrayList<FileStructure> actual = files;

        if (data.exists()) {
            try {
                is = new FileInputStream(data);

                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();

                while(line != null){

                    FileStructure struct = new FileStructure();

                    int level = Integer.parseInt(line.substring(0, line.indexOf(':')));
                    struct.title = line.substring(line.indexOf('"')+1, line.lastIndexOf('"'));
                    if (line.indexOf(':') == line.lastIndexOf(':')){
                        struct.files = new ArrayList<>();
                    }
                    else {
                        struct.url = line.substring(line.lastIndexOf(':')+1, line.length());
                    }

                    if (lastLevel > level){
                        actual = files;
                        for (int j = 0; j < level; j++){
                            actual = actual.get(actual.size()-1).files;
                        }
                        lastLevel = level;

                    }
                    else if (lastLevel < level){
                        actual = actual.get(actual.size()-1).files;
                        lastLevel = level;
                    }

                    actual.add(struct);

                    line = reader.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillView(){

        scrollLayout.removeAllViewsInLayout();
        adapters.clear();

        for (int i = 0; i < openedLevel; i++){
            final int l = i;
            adapters.add(new FileAdapter(getContext(), R.layout.aip_list_item, getOpenedLevel(i+1), i+1));
            ListView list = (ListView) inflater.inflate(R.layout.aip_list, scrollLayout, false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (screenWidth*0.8), ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.aip_list_margin), 0);
            list.setLayoutParams(params);

            list.setAdapter(adapters.get(i));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l1) {
                    handleOnItemClick(l, position);
                }
            });

            scrollLayout.addView(list);
        }

        horizontalScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);

    }

    private void handleOnItemClick(int lvl, int pos){
        FileStructure struct = getOpenedLevel(lvl+1).get(pos);
        if (struct.files == null){
            PdfViewerDialog viewer = new PdfViewerDialog(getContext(), R.style.FullScreenDialog);
            viewer.setContentView(R.layout.pdf_viewer);
            viewer.loadContent(struct.title, folder.getPath(), struct.url);
            viewer.show();
        }
        else {
            while (selectedItems.size() > lvl)
                selectedItems.remove(selectedItems.size()-1);

            selectedItems.add(pos);
            openedLevel = selectedItems.size() + 1;

            fillView();
        }

    }

    private ArrayList<FileStructure> getOpenedLevel(int l){
        ArrayList<FileStructure> level = files;

        for(int i = 1; i < l; i++){
            level = level.get(selectedItems.get(i-1)).files;
        }

        return level;
    }

    private static class FileStructure {
        public String title = null;
        public String url = null;
        public ArrayList<FileStructure> files = null;

    }

    private class FileAdapter extends ArrayAdapter<FileStructure> {

        private Context context;
        private int layoutResourceId;
        private ArrayList<FileStructure> data = null;
        private int level;

        public FileAdapter(Context c, int layoutR, ArrayList<FileStructure> d, int l){
            super(c, layoutR, d);
            context = c;
            layoutResourceId = layoutR;
            data = d;
            level = l;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            TextView title = (TextView) row.findViewById(R.id.aip_row_title);
            TextView description = (TextView) row.findViewById(R.id.aip_row_description);

            int pos = data.get(position).title.indexOf('-');
            int len = data.get(position).title.length();

            //Log.e("title", data.get(position).title.substring(0,pos));
            //Log.w("dsc", data.get(position).title.substring(pos+1,len));

            title.setText(data.get(position).title.substring(0,pos));
            description.setText(data.get(position).title.substring(pos+2,len)); //+2 for the empty space, maybe only for czech AIP

            ImageView icon = (ImageView) row.findViewById(R.id.aip_row_icon);
            if (data.get(position).files == null)
                icon.setImageResource(R.drawable.ic_docs_inv);

            if (selectedItems.size() >= level && data.get(position).files != null)
                if (position == selectedItems.get(level-1))
                    row.setBackgroundResource(R.drawable.bck_item_selected);
                else
                    row.setBackgroundResource(R.drawable.bck_transparent);

            return row;
        }

    }
}