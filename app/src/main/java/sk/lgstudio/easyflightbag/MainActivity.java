package sk.lgstudio.easyflightbag;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

import sk.lgstudio.easyflightbag.ui.CustomFragmentAdapter;
import sk.lgstudio.easyflightbag.ui.CustomViewPager;
import sk.lgstudio.easyflightbag.ui.FragmentAip;
import sk.lgstudio.easyflightbag.ui.FragmentCalc;
import sk.lgstudio.easyflightbag.ui.FragmentChklist;
import sk.lgstudio.easyflightbag.ui.FragmentDocs;
import sk.lgstudio.easyflightbag.ui.FragmentHome;
import sk.lgstudio.easyflightbag.ui.FragmentPlan;
import sk.lgstudio.easyflightbag.ui.FragmentSettings;
import sk.lgstudio.easyflightbag.ui.FragmentWeather;
import sk.lgstudio.easyflightbag.ui.TabMenu;

import static android.os.Environment.getDataDirectory;

public class MainActivity extends AppCompatActivity {

    private TabMenu menu;

    private CustomViewPager viewPager;

    private File rootDir;
    private File chkDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkDirectory();
        initView();

    }

    private void checkDirectory(){
        rootDir = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_root));
        if(!rootDir.exists()) {
            rootDir.mkdir();
        }

        chkDir = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_chklist));
        if(!chkDir.exists()) {
            chkDir.mkdir();
        }

    }

    private void initView(){
        final CustomFragmentAdapter fA = new CustomFragmentAdapter(getSupportFragmentManager());
        fA.addFragment(new FragmentHome());
        fA.addFragment(new FragmentAip());
        fA.addFragment(new FragmentWeather());
        fA.addFragment(new FragmentChklist());
        fA.addFragment(new FragmentDocs());
        fA.addFragment(new FragmentPlan());
        fA.addFragment(new FragmentCalc());
        fA.addFragment(new FragmentSettings());

        viewPager = (CustomViewPager) findViewById(R.id.view_fragment_pager);
        viewPager.setAdapter(fA);
        viewPager.setPagingEnabled(false);

        menu = new TabMenu(this, viewPager);
    }

    @Override
    public void onBackPressed() {

        if (menu.selected != 0)
            menu.change(0);
        else
            super.onBackPressed();
    }

}
