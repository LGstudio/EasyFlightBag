package sk.lgstudio.easyflightbag.menu;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Gabriel Lehocky
 *
 * Adapter for fragments in .MainAvtivity
 */
public class TabFragmentAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> fragments;

    /**
     *
     */
    public TabFragmentAdapter(FragmentManager fm) {
        super(fm);

        fragments = new ArrayList<>();
    }

    public void addFragment(Fragment f){
        fragments.add(f);

    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}
