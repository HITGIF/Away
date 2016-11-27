/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.fragments;


import android.app.Fragment;
import android.app.TimePickerDialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
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
import android.widget.TimePicker;

import com.balysv.materialmenu.MaterialMenuDrawable;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.Date;

import carbonylgroup.away.R;
import carbonylgroup.away.classes.HistoryHandler;

public class DetailFragment extends Fragment {

    private int white;
    private int primaryDark;
    private int primary;
    private int accent;
    private long totalTime;
    private long completedTime;

    private Animation edit_fab_in;
    private HistoryHandler historyHandler;

    private View view;
    private PieChart detail_pie_chart;
    private TextView total_time_tv;
    private TextView completed_time_tv;
    private TimePickerDialog timePickerDialog;
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

        historyHandler = new HistoryHandler(getActivity());

        total_time_tv = (TextView) view.findViewById(R.id.total_time_tv);
        completed_time_tv = (TextView) view.findViewById(R.id.completed_time_tv);

        detail_pie_chart = (PieChart) view.findViewById(R.id.detail_pie_chart);
        edit_fab = (FloatingActionButton) view.findViewById(R.id.edit_fab);
        edit_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        historyHandler.setGoal(hourOfDay, minute);
                    }
                };
                timePickerDialog = new TimePickerDialog(getActivity(), onTimeSetListener, 0, 0, true);
                timePickerDialog.show();
            }
        });

        initTime();
        initColor();
    }

    private void initUI() {

        initTextViews();
        initPieChart();
    }

    private void initAnim() {

        edit_fab_in = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        edit_fab_in.setDuration(200);
        edit_fab_in.setStartOffset(200);
        edit_fab_in.setInterpolator(new DecelerateInterpolator());

        displayViewAnimation();
    }

    private void initTime(){

        long todayTime;
        totalTime = historyHandler.getGoalInNum();
        todayTime = historyHandler.getTimeOfDay(new Date());
        completedTime = todayTime > totalTime ? totalTime : todayTime;
    }

    private void initColor() {

        white = getResources().getColor(R.color.white);
        primaryDark = getResources().getColor(R.color.colorPrimaryDark);
        primary = getResources().getColor(R.color.colorPrimary);
        accent = getResources().getColor(R.color.colorAccent);
    }

    private void initTextViews() {

        total_time_tv.setText(historyHandler.getGoalInStr());
        completed_time_tv.setText(historyHandler.S2HM(completedTime));
    }

    private void initPieChart() {

        detail_pie_chart.clearChart();
        detail_pie_chart.addPieSlice(new PieModel("Completed", completedTime, accent));
        detail_pie_chart.addPieSlice(new PieModel("Incomplete", totalTime - completedTime, primaryDark));
        detail_pie_chart.setAnimationTime(1000);
        detail_pie_chart.startAnimation();
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