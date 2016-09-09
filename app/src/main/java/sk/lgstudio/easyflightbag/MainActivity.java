package sk.lgstudio.easyflightbag;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

public class MainActivity extends AppCompatActivity {

    private TabMenu menu;

    private CustomViewPager viewPager;
    private FragmentHome fHome = new FragmentHome();
    private FragmentAip fAip = new FragmentAip();
    private FragmentWeather fWeather = new FragmentWeather();
    private FragmentChklist fChklist = new FragmentChklist();
    private FragmentDocs fDocs = new FragmentDocs();
    private FragmentPlan fPlan = new FragmentPlan();
    private FragmentCalc fCalc = new FragmentCalc();
    private FragmentSettings fSettings = new FragmentSettings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView(){
        final CustomFragmentAdapter fA = new CustomFragmentAdapter(getSupportFragmentManager());
        fA.addFragment(fHome);
        fA.addFragment(fAip);
        fA.addFragment(fWeather);
        fA.addFragment(fChklist);
        fA.addFragment(fDocs);
        fA.addFragment(fPlan);
        fA.addFragment(fCalc);
        fA.addFragment(fSettings);

        viewPager = (CustomViewPager) findViewById(R.id.view_fragment_pager);
        viewPager.setAdapter(fA);
        viewPager.setPagingEnabled(false);

        menu = new TabMenu(this, viewPager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
