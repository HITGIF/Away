/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuView;

import carbonylgroup.away.R;
import carbonylgroup.away.classes.DetailsTransition;
import carbonylgroup.away.classes.TransitionHelper;
import carbonylgroup.away.fragments.DashboardFragment;
import carbonylgroup.away.fragments.DetailFragment;
import carbonylgroup.away.fragments.HomeFragment;


public class MainActivity extends TransitionHelper.MainActivity implements NavigationView.OnNavigationItemSelectedListener {

    public View fragmentBackground;

    private boolean menuOpenDrawer = true;
    private int presentFragment;
    private Toolbar mainToolBar;
    private DrawerLayout drawer;
    private MaterialMenuView materialMenu;
    private MaterialMenuDrawable.IconState currentIconState;

    /**
     * Fragments
     **/
    private HomeFragment homeFragment;
    private DetailFragment detailFragment;
    private DashboardFragment dashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer);

        initValue();
        initUI();
        initOnClick();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        presentFragment = savedInstanceState.getInt("presentFragment");
        int[] fragments = {R.id.nav_home, R.id.detail_background, R.id.nav_dashboard};
        initFragmentWithMenuItemId(fragments[presentFragment]);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt("presentFragment", presentFragment);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (presentFragment == 1)
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                returnToDashBoard();
                return true;
            }
        return super.onKeyDown(keyCode, event);
    }

    private void initValue() {

        fragmentBackground = findViewById(R.id.base_fragment_background);
        mainToolBar = (Toolbar) findViewById(R.id.main_toolbar);
        materialMenu = (MaterialMenuView) findViewById(R.id.material_menu_view);
    }

    private void initUI() {

        initDrawer();
        setDefaultFragment();
    }

    private void initOnClick() {

        materialMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuOpenDrawer)
                    drawer.openDrawer(GravityCompat.START);
                else
                    returnToDashBoard();
            }
        });
    }

    private void initDrawer() {

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void initFragmentWithMenuItemId(int id) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        switch (id) {

            case R.id.nav_home:

                if (homeFragment == null)
                    homeFragment = new HomeFragment();
                transaction.replace(R.id.content_view, homeFragment);
                presentFragment = 0;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    setToolBarElevation(0);
                break;

            case R.id.detail_background:

                if (detailFragment == null)
                    detailFragment = new DetailFragment();
                transaction.replace(R.id.content_view, detailFragment);
                presentFragment = 1;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    setToolBarElevation(0);
                break;

            case R.id.nav_dashboard:

                if (dashboardFragment == null)
                    dashboardFragment = new DashboardFragment();
                transaction.replace(R.id.content_view, dashboardFragment);
                presentFragment = 2;

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                    setToolBarElevation(10);
                break;

            default:
                break;
        }

        transaction.commit();
    }

    private void setDefaultFragment() {

        int fragmentResourceId = getIntent().getIntExtra("fragment_resource_id", R.layout.home_view_content);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        switch (fragmentResourceId) {
            case R.layout.home_view_content:
                initFragmentWithMenuItemId(R.id.nav_home);
                break;
            case R.layout.detail_view_content:
                initFragmentWithMenuItemId(R.layout.detail_view_content);
                break;
        }
        transaction.commit();
    }

    public void returnToDashBoard() {

        if (dashboardFragment == null)
            dashboardFragment = new DashboardFragment();
        dashboardFragment.setSharedElementEnterTransition(new DetailsTransition());
        dashboardFragment.setSharedElementReturnTransition(new DetailsTransition());


        getFragmentManager()
                .beginTransaction()
                .addSharedElement(findViewById(R.id.detail_background), getString(R.string.transition_name_goal_view))
                .replace(R.id.content_view, dashboardFragment)
                .addToBackStack(null)
                .commit();

        animateHomeIcon(MaterialMenuDrawable.IconState.BURGER, true);
        setToolBarElevation(0);
    }

    public void enableDrawer(boolean enable) {

        if (enable)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        else
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void setHomeIcon(MaterialMenuDrawable.IconState iconState) {
        if (currentIconState == iconState) return;
        currentIconState = iconState;
        materialMenu.setState(currentIconState);
    }

    public boolean animateHomeIcon(MaterialMenuDrawable.IconState iconState, boolean openDrawer) {
        menuOpenDrawer = openDrawer;
        enableDrawer(openDrawer);
        if (currentIconState == iconState) return false;
        currentIconState = iconState;
        materialMenu.animateState(currentIconState);
        return true;
    }

    public int getPresentFragment(){
        return presentFragment;
    }

    public void setPresentFragment(int input){
        presentFragment = input;
    }

    public void setToolBarElevation(int toolBarElevation) {
        mainToolBar.setElevation(toolBarElevation);
    }

    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        initFragmentWithMenuItemId(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static MainActivity of(Activity activity) {
        return (MainActivity) activity;
    }

}