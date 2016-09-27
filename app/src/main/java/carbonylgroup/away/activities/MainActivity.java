/** Copyright (C) 2016 Gustav Wang */

package carbonylgroup.away.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;


import carbonylgroup.away.R;
import carbonylgroup.away.custom_palette.CircleSeekBar;
import carbonylgroup.away.custom_palette.DrawerArrowDrawable;

public class MainActivity extends Activity {

    private float offset;

    private Animation start_oc;
    private Animation start_or;

    private Button start_bt;
    private ImageView imageView;

    private DrawerArrowDrawable drawerArrowDrawable;
    private DrawerLayout drawer;
    private CircleSeekBar circleSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initValue();
        initUI();
        initAnim();
        initOnClick();
    }

    private void initValue() {

        Resources resources = getResources();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        start_bt = (Button) findViewById(R.id.start_bt);
        imageView = (ImageView) findViewById(R.id.drawer_indicator);
        circleSeekBar = (CircleSeekBar) findViewById(R.id.circle_seekbar);

        circleSeekBar.setProgress(0);
        circleSeekBar.setProgressFrontColor(Color.LTGRAY);

        drawerArrowDrawable = new DrawerArrowDrawable(resources);
        drawerArrowDrawable.setStrokeColor(resources.getColor(R.color.light_gray));

        imageView.setImageDrawable(drawerArrowDrawable);
    }

    private void initUI() {

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/futura_lt_light.ttf");
        start_bt.setTypeface(typeFace);
        ((Button) findViewById(R.id.back_start)).setTypeface(typeFace);
    }

    private void initAnim() {

        start_oc = AnimationUtils.loadAnimation(this, R.anim.start_button_onclick);
        start_oc.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                start_bt.setAlpha(0);
            }
        });

        start_or = AnimationUtils.loadAnimation(this, R.anim.start_button_onrelease);
        start_or.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                start_bt.setAlpha(1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
    }

    private void initOnClick() {

        drawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                offset = slideOffset;

                // Sometimes slideOffset ends up so close to but not quite 1 or 0.
                if (slideOffset >= .995) {
                    drawerArrowDrawable.setFlip(true);
                } else if (slideOffset <= .005) {
                    drawerArrowDrawable.setFlip(false);
                }

                drawerArrowDrawable.setParameter(offset);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerVisible(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        start_bt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        start_bt.startAnimation(start_oc);
                        break;
                    case MotionEvent.ACTION_UP:
                        start_bt.startAnimation(start_or);
                        int value = getTimeSeek();
                        Intent intent = new Intent();
                        intent.putExtra("time", value);
                        intent.setClass(MainActivity.this, OnTimeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }

    private int getTimeSeek() {

        int timeInValue = circleSeekBar.getProgress();
        int hours = timeInValue / 12;
        int minutes = timeInValue % 12 * 5;
        return hours * 3600 + minutes * 60;
    }

}
