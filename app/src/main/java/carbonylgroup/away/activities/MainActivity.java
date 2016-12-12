/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuView;

import java.util.Locale;

import carbonylgroup.away.R;
import carbonylgroup.away.classes.DetailsTransition;
import carbonylgroup.away.classes.HistoryHandler;
import carbonylgroup.away.classes.TransitionHelper;
import carbonylgroup.away.fragments.DashboardFragment;
import carbonylgroup.away.fragments.DetailFragment;
import carbonylgroup.away.fragments.HomeFragment;



public class MainActivity extends TransitionHelper.MainActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean menuOpenDrawer = true;
    private int presentFragment;

    private Toolbar mainToolBar;
    private DrawerLayout drawer;
    private AlertDialog optionsDialog;
    private HistoryHandler historyHandler;
    private MaterialMenuView materialMenu;
    private MaterialMenuDrawable.IconState currentIconState;
    private AdapterView.OnItemClickListener setThemeOC;
    private AdapterView.OnItemClickListener setLanguageOC;

    /* Fragments */
    private HomeFragment homeFragment;
    private DetailFragment detailFragment;
    private DashboardFragment dashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        initBase();
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
        gotoFragmentWithMenuItemId(fragments[presentFragment]);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt("presentFragment", presentFragment);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (presentFragment) {

            case 1:
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    returnToDashBoard();
                    return true;
                }
                break;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /* Initializer */
    private void initBase() {

        historyHandler = new HistoryHandler(MainActivity.this);
        setTheme(historyHandler.getThemeIdNow());
        setLanguage(historyHandler.getLanguageIdNow());
    }

    private void initValue() {

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
                if (menuOpenDrawer) drawer.openDrawer(GravityCompat.START);
                else returnToDashBoard();
            }
        });


        setThemeOC = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                historyHandler.setThemeIdNow(position);
                showThemeDialog(false);
                restartSelf();
            }
        };

        setLanguageOC = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                historyHandler.setLanguageIdNow(position);
                showLanguageDialog(false);
                restartSelf();
            }
        };
    }

    private void initDrawer() {

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initThemeDialog() {

        AlertDialog.Builder themeDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        ListView color_list_view;
        LayoutInflater inflater = getLayoutInflater();
        View optionsDialogView = inflater.inflate(R.layout.options_dialog_view, (ViewGroup) findViewById(R.id.color_root_view));
        ((TextView)optionsDialogView.findViewById(R.id.options_dialog_title)).setText(getString(R.string.choose_your_favorite_color));
        SimpleAdapter adapter = new SimpleAdapter(this, historyHandler.getThemeData(), R.layout.color_list_item,
                new String[]{"title", "selectedTitle", "img", "cover", "checked"},
                new int[]{R.id.color_title, R.id.color_title_selected, R.id.color_img, R.id.color_img_cover, R.id.checkbox_img});

        color_list_view = (ListView) optionsDialogView.findViewById(R.id.options_list_view);
        color_list_view.setOnItemClickListener(setThemeOC);
        color_list_view.setAdapter(adapter);
        themeDialogBuilder.setView(optionsDialogView);
        optionsDialog = themeDialogBuilder.create();
        optionsDialog.show();
    }

    private void initLanguageDialog() {

        AlertDialog.Builder languageDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        ListView language_list_view;
        LayoutInflater inflater = getLayoutInflater();
        View optionsDialogView = inflater.inflate(R.layout.options_dialog_view, (ViewGroup) findViewById(R.id.color_root_view));
        ((TextView)optionsDialogView.findViewById(R.id.options_dialog_title)).setText(getString(R.string.choose_your_preferred_language));
        SimpleAdapter adapter = new SimpleAdapter(this, historyHandler.getLanguageData(), R.layout.language_list_item,
                new String[]{"title", "selectedTitle", "img", "checked"},
                new int[]{R.id.language_title, R.id.language_title_selected, R.id.language_img, R.id.language_checkbox_img});

        language_list_view = (ListView) optionsDialogView.findViewById(R.id.options_list_view);
        language_list_view.setOnItemClickListener(setLanguageOC);
        language_list_view.setAdapter(adapter);
        languageDialogBuilder.setView(optionsDialogView);
        optionsDialog = languageDialogBuilder.create();
        optionsDialog.show();
    }

    /* Fragments Handler */
    public void gotoFragmentWithMenuItemId(int id) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        switch (id) {

            case R.id.nav_home:

                if (homeFragment == null) homeFragment = new HomeFragment();
                transaction.replace(R.id.content_view, homeFragment);
                presentFragment = 0;
                break;

            case R.id.detail_background:

                if (detailFragment == null) detailFragment = new DetailFragment();
                transaction.replace(R.id.content_view, detailFragment);
                presentFragment = 1;
                break;

            case R.id.nav_dashboard:

                if (dashboardFragment == null) dashboardFragment = new DashboardFragment();
                transaction.replace(R.id.content_view, dashboardFragment);
                presentFragment = 2;
                break;

            case R.id.nav_theme:

                showThemeDialog(true);
                break;

            case R.id.nav_language:

                showLanguageDialog(true);
                break;

            default:
                break;
        }

        transaction.commit();
    }

    public void returnToDashBoard() {

        if (dashboardFragment == null) dashboardFragment = new DashboardFragment();
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

    public int getPresentFragment() {
        return presentFragment;
    }

    public void setPresentFragment(int input) {
        presentFragment = input;
    }


    public void setToolBarElevation(int toolBarElevation) {
        mainToolBar.setElevation(toolBarElevation);
    }

    private void setDefaultFragment() {

        int fragmentResourceId = getIntent().getIntExtra("fragment_resource_id", R.layout.home_view_content);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        switch (fragmentResourceId) {
            case R.layout.home_view_content:
                gotoFragmentWithMenuItemId(R.id.nav_home);
                break;
            case R.layout.detail_view_content:
                gotoFragmentWithMenuItemId(R.layout.detail_view_content);
                break;
        }
        transaction.commit();
    }

    /* Other Methods */
    public boolean animateHomeIcon(MaterialMenuDrawable.IconState iconState, boolean openDrawer) {

        menuOpenDrawer = openDrawer;
        enableDrawer(openDrawer);
        if (currentIconState == iconState) return false;
        currentIconState = iconState;
        materialMenu.animateState(currentIconState);
        return true;
    }

    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        gotoFragmentWithMenuItemId(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void enableDrawer(boolean enable) {

        if (enable) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        else drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public static MainActivity of(Activity activity) {
        return (MainActivity) activity;
    }

    private void showThemeDialog(boolean toShow) {

        if (toShow) initThemeDialog();
        else optionsDialog.dismiss();
    }

    private void showLanguageDialog(boolean toShow) {

        if (toShow) initLanguageDialog();
        else optionsDialog.dismiss();
    }

    private void setLanguage(Locale language) {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Configuration configuration = getResources().getConfiguration();
        configuration.locale = language;
        getResources().updateConfiguration(configuration, displayMetrics);
    }

    private void restartSelf() {

        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
    }

}