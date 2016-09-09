package sk.lgstudio.easyflightbag.ui;

import sk.lgstudio.easyflightbag.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

/**
 *
 */
public class TabMenu implements View.OnClickListener{

    private ArrayList<ImageButton> tabs = new ArrayList<>();
    private int selected = 0;
    private CustomViewPager viewPager;

    public TabMenu(Activity a, CustomViewPager vP){

        viewPager = vP;

        tabs.add((ImageButton) a.findViewById(R.id.tab_home));
        tabs.add((ImageButton) a.findViewById(R.id.tab_aip));
        tabs.add((ImageButton) a.findViewById(R.id.tab_weather));
        tabs.add((ImageButton) a.findViewById(R.id.tab_chklist));
        tabs.add((ImageButton) a.findViewById(R.id.tab_docs));
        tabs.add((ImageButton) a.findViewById(R.id.tab_plan));
        tabs.add((ImageButton) a.findViewById(R.id.tab_calc));
        tabs.add((ImageButton) a.findViewById(R.id.tab_set));

        for (ImageButton ib : tabs){
            ib.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int clicked = 0;
        switch (v.getId()){
            case R.id.tab_home:
                clicked = 0;
                break;
            case R.id.tab_aip:
                clicked = 1;
                break;
            case R.id.tab_weather:
                clicked = 2;
                break;
            case R.id.tab_chklist:
                clicked = 3;
                break;
            case R.id.tab_docs:
                clicked = 4;
                break;
            case R.id.tab_plan:
                clicked = 5;
                break;
            case R.id.tab_calc:
                clicked = 6;
                break;
            case R.id.tab_set:
                clicked = 7;
                break;
        }
        change(clicked);
    }

    private void change(int clicked){
        tabs.get(selected).setBackgroundResource(R.color.colorTransparent);
        tabs.get(selected).setAlpha((float) 0.5);
        tabs.get(clicked).setBackgroundResource(R.drawable.bck_tab_selected);
        tabs.get(clicked).setAlpha((float) 1.0);
        viewPager.setCurrentItem(clicked, false);
        selected = clicked;
    }
}
