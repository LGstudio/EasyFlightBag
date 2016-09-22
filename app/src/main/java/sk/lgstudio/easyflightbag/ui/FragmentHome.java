package sk.lgstudio.easyflightbag.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import sk.lgstudio.easyflightbag.R;

/**
 *
 */
public class FragmentHome extends Fragment implements View.OnClickListener {

    private ImageButton panelTopBtn;
    private ImageButton panelBottomBtn;
    private RelativeLayout panelTop;
    private RelativeLayout panelBottom;
    private LinearLayout fullLayout;
    private LinearLayout mapLayout;

    private boolean isPanelTop = true;
    private boolean isPanelBottom = true;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            isPanelTop = savedInstanceState.getBoolean("PTop");
            isPanelBottom = savedInstanceState.getBoolean("PBottom");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("PTop", isPanelTop);
        outState.putBoolean("PBottom", isPanelBottom);

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        panelTopBtn = (ImageButton) view.findViewById(R.id.home_panel_top_btn);
        panelBottomBtn = (ImageButton) view.findViewById(R.id.home_panel_bottom_btn);

        panelTopBtn.setOnClickListener(this);
        panelBottomBtn.setOnClickListener(this);

        panelTop = (RelativeLayout) view.findViewById(R.id.home_panel_top);
        panelBottom = (RelativeLayout) view.findViewById(R.id.home_panel_bottom);
        mapLayout = (LinearLayout) view.findViewById(R.id.home_panel_map);
        fullLayout = (LinearLayout) view.findViewById(R.id.home_screen);

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();

        //changePanelState(isPanelTop, isPanelBottom);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.home_panel_top_btn:
                changePanelState(!isPanelTop, isPanelBottom);
                break;
            case R.id.home_panel_bottom_btn:
                changePanelState(isPanelTop, !isPanelBottom);
                break;
        }
    }

    private void changePanelState(boolean top, boolean bottom){
        isPanelTop = top;
        isPanelBottom = bottom;

        int panels = 0;
        int open = fullLayout.getHeight() / 4;
        int close = panelTopBtn.getHeight();

        if (isPanelTop){
            panels += open;
            panelTop.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, open));
            panelTopBtn.setImageResource(R.drawable.ic_expand_up_inv);
        }
        else {
            panels += close;
            panelTop.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, close));
            panelTopBtn.setImageResource(R.drawable.ic_expand_down_inv);
        }

        if (isPanelBottom){
            panels += open;
            panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, open));
            panelBottomBtn.setImageResource(R.drawable.ic_expand_down_inv);
        }
        else {
            panels += close;
            panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, close));
            panelBottomBtn.setImageResource(R.drawable.ic_expand_up_inv);
        }

        int mapH = fullLayout.getHeight() - panels;
        mapLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mapH));

    }
}
