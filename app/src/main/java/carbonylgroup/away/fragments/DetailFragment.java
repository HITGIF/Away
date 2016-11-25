/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.fragments;


import android.app.Fragment;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import carbonylgroup.away.R;
import carbonylgroup.away.activities.MainActivity;
import carbonylgroup.away.classes.BitmapUtil;
import carbonylgroup.away.classes.DetailsTransition;
import carbonylgroup.away.classes.TransitionHelper;


public class DetailFragment extends Fragment {

    private int white;
    private int primaryDark;
    private int primary;
    private int accent;
    private View view;
    private Animation edit_fab_in;
    private FloatingActionButton edit_fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.detail_view_content, container, false);

        initAnim();
        initValue();
        initUI();


        return view;
    }

    private void initValue() {

        edit_fab = (FloatingActionButton) view.findViewById(R.id.edit_fab);
        initColor();
    }

    private void initUI() {


    }

    private void initColor() {

        white = getResources().getColor(R.color.white);
        primaryDark = getResources().getColor(R.color.colorPrimaryDark);
        primary = getResources().getColor(R.color.colorPrimary);
        accent = getResources().getColor(R.color.colorAccent);
    }

    private void initAnim() {

        edit_fab_in = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        edit_fab_in.setDuration(200);
        edit_fab_in.setStartOffset(200);
        edit_fab_in.setInterpolator(new DecelerateInterpolator());

        displayViewAnimation();
    }

    private void displayViewAnimation() {

        final ScrollView bg = (ScrollView) view.findViewById(R.id.detail_background);
        ViewTreeObserver vto2 = bg.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bg.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                edit_fab.startAnimation(edit_fab_in);
            }
        });
    }


}