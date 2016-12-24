/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import carbonylgroup.away.R;
import carbonylgroup.away.activities.OnTimeActivity;
import carbonylgroup.away.custom_palette.CircleSeekBar;


public class HomeFragment extends Fragment {

    private Animation start_bt_in;
    private FloatingActionButton start_bt;
    private CircleSeekBar circleSeekBar;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.home_view_content, container, false);

        initValue();
        initOnClick();
        initAnim();

        return view;
    }

    private void initValue() {

        start_bt = (FloatingActionButton) view.findViewById(R.id.start_bt);
        circleSeekBar = (CircleSeekBar) view.findViewById(R.id.circle_seekbar);
        circleSeekBar.setProgress(0);
    }

    private void initOnClick() {

        start_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int value = getTimeSeek();
                Intent intent = new Intent();
                intent.putExtra("time", value);
                intent.setClass(getActivity(), OnTimeActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
            }
        });
    }

    private void initAnim() {

        start_bt_in = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        start_bt_in.setDuration(200);
        start_bt_in.setInterpolator(new DecelerateInterpolator());

        displayViewAnimation();
    }

    private void displayViewAnimation() {

        ViewTreeObserver vto2 = circleSeekBar.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                circleSeekBar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                circleSeekBar.requestLayout();
                Thread s = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        circleSeekBar.postInvalidate();
                    }
                });
                s.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    animateRevealShow(view);
            }
        });
    }

    private int getTimeSeek() {

        int timeInValue = circleSeekBar.getProgress();
        int hours = timeInValue / 12;
        int minutes = timeInValue % 12 * 5;
        return hours * 3600 + minutes * 60;
    }

    @TargetApi(21)
    public void animateRevealShow(final View viewRoot) {

        int cx = (int) viewRoot.getX();
        int cy = (int) viewRoot.getY();
        int radius = viewRoot.getHeight() + viewRoot.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, radius);
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                start_bt.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                start_bt.setVisibility(View.VISIBLE);
                start_bt.startAnimation(start_bt_in);
            }
        });
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(800);
        anim.start();
    }

}