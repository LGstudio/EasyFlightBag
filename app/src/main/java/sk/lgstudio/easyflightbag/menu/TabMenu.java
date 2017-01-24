package sk.lgstudio.easyflightbag.menu;

import sk.lgstudio.easyflightbag.R;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

/**
 *
 */
public class TabMenu implements View.OnClickListener{

    private final int[] ids = {
            R.id.tab_home,
            R.id.tab_calc,
            R.id.tab_aip,
            R.id.tab_chklist,
            R.id.tab_docs,
            R.id.tab_set
    };

    private ArrayList<ImageButton> tabs = new ArrayList<>();
    public int selected = 0;
    private TabViewPager viewPager;

    public TabMenu(Activity a, TabViewPager vP){

        viewPager = vP;

        for (int id : ids)
            tabs.add((ImageButton) a.findViewById(id));

        for (ImageButton ib : tabs)
            ib.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int clicked = 0;
        for (int i = 0; i < ids.length; i++)
            if (ids[i] == v.getId())
                clicked = i;

        if (selected != clicked)
            change(clicked);
    }

    public void change(int clicked){
        tabs.get(selected).setBackgroundResource(R.color.colorTransparent);
        tabs.get(selected).setAlpha((float) 0.5);
        tabs.get(clicked).setBackgroundResource(R.drawable.bck_tab_selected);
        tabs.get(clicked).setAlpha((float) 1.0);
        viewPager.setCurrentItem(clicked, false);
        selected = clicked;
    }
}
