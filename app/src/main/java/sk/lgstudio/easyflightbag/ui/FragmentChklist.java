package sk.lgstudio.easyflightbag.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import sk.lgstudio.easyflightbag.R;

/**
 *
 */
public class FragmentChklist extends Fragment {

    private ImageButton check;
    private ListView listFiles;
    private ListView listContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chklist, container, false);

        check = (ImageButton) view.findViewById(R.id.chklist_done);
        listFiles = (ListView) view.findViewById(R.id.chklist_list_content);
        listContent = (ListView) view.findViewById(R.id.chklist_list_files);

        return view;
    }

    private class FileAdapter extends ArrayAdapter<String>{

        Context context;
        int layoutResourceId;
        String data[] = null;

        public FileAdapter(Context c, int layoutR, String[] d){
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
            else{
                filename = (TextView) row.getTag();
            }

            filename.setText(data[position]);

            return row;
        }

    }

}