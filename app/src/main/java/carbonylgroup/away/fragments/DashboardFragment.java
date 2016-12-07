/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.AppCompatSpinner;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.util.ArrayList;
import java.util.Date;

import carbonylgroup.away.R;
import carbonylgroup.away.activities.MainActivity;
import carbonylgroup.away.classes.DetailsTransition;
import carbonylgroup.away.classes.HistoryHandler;


import static android.support.v7.appcompat.R.attr.colorAccent;
import static carbonylgroup.away.R.attr.colorPrimary;
import static carbonylgroup.away.R.attr.colorPrimaryDark;


public class DashboardFragment extends Fragment {

    private HistoryHandler.timePeriod timePeriod;
    private String[] legendForWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private String[] legendInNum = {
            "01", "02", "03", "04", "05", "06",
            "07", "08", "09", "10", "11", "12",
            "13", "14", "15", "16", "17", "18",
            "19", "20", "21", "22", "23", "24",
            "25", "26", "27", "28", "29", "30", "31"};

    private int white;
    private int primaryDark;
    private int primary;
    private int accent;
    private long totalTime;
    private long completedTime;

    private HistoryHandler historyHandler;
    private Animation daily_goal_title_fade_in;

    private View view;
    private TextView today_earned_hour;
    private TextView today_earned_min;
    private TextView goal_percentage_tv;
    private TextView total_earned_time_tv;
    private TextView total_earned_unit_tv;
    private CardView card_view_today;
    private CardView goal_card_view;
    private CardView total_card_view;
    private CardView bar_chart_card_view;
    private CardView overview_card_view;
    private PieChart mPieChart;
    private BarChart mBarChart;
    private ValueLineChart mCubicValueLineChart;

    private DetailFragment detailFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.dash_board_view_content, container, false);

        initValue();
        initAnim();

        return view;
    }

    @Override
    public void onDestroyView() {
        MainActivity.of(getActivity()).setToolBarElevation(0);
        super.onDestroyView();
    }

    private void initValue() {

        historyHandler = new HistoryHandler(getActivity());
        timePeriod = HistoryHandler.timePeriod.TODAY;

        initTime();
        initColor();
        initViews();
        initCharts();
    }

    private void initAnim() {

        daily_goal_title_fade_in = new AlphaAnimation(0f, 1.0f);
        daily_goal_title_fade_in.setDuration(300);
        daily_goal_title_fade_in.setStartOffset(400);

        if (MainActivity.of(getActivity()).getPresentFragment() != 1)
            displayViewAnimation();
        else
            MainActivity.of(getActivity()).setPresentFragment(2);

        MainActivity.of(getActivity()).setToolBarElevation(10);
    }

    private void displayViewAnimation() {

        ViewTreeObserver vto2 = goal_card_view.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                goal_card_view.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setCardsInvisible();
                    animateRevealShow(card_view_today, 0, 0);
                    animateRevealShow(goal_card_view, 150, R.id.pieChart);
                    animateRevealShow(total_card_view, 300, 0);
                    animateRevealShow(bar_chart_card_view, 450, R.id.barChart);
                    animateRevealShow(overview_card_view, 600, R.id.cubicLineChart);
                }
            }
        });
    }

    private void initTime() {

        long todayTime;
        totalTime = historyHandler.getGoalInNum(0);
        todayTime = historyHandler.getTimeOfDay(new Date());
        completedTime = todayTime > totalTime ? totalTime : todayTime;
    }

    private void initColor() {

        white = getResources().getColor(R.color.white);

        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        primaryDark = typedValue.data;
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primary = typedValue.data;
        getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        accent = typedValue.data;
    }

    private void initViews() {

        card_view_today = (CardView) view.findViewById(R.id.card_view_today);
        goal_card_view = (CardView) view.findViewById(R.id.goal_card_view);
        total_card_view = (CardView) view.findViewById(R.id.total_card_view);
        bar_chart_card_view = (CardView) view.findViewById(R.id.bar_chart_card_view);
        overview_card_view = (CardView) view.findViewById(R.id.overview_card_view);
        today_earned_hour = (TextView) view.findViewById(R.id.today_earned_hour);
        today_earned_min = (TextView) view.findViewById(R.id.today_earned_min);
        goal_percentage_tv = (TextView) view.findViewById(R.id.goal_percentage_tv);
        total_earned_time_tv = (TextView) view.findViewById(R.id.total_earned_time_tv);
        total_earned_unit_tv = (TextView) view.findViewById(R.id.total_earned_unit_tv);

        goal_card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoDailyGoalDetail();
            }
        });


        AppCompatSpinner time_period_spinner = (AppCompatSpinner) view.findViewById(R.id.time_period_spinner);
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.time_period_spinner_entries, R.layout.custom_spinner);
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_drop_item);
        time_period_spinner.setAdapter(spinnerAdapter);
        time_period_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        timePeriod = HistoryHandler.timePeriod.TODAY;
                        initLineChart();
                        mCubicValueLineChart.startAnimation();
                        break;
                    case 1:
                        timePeriod = HistoryHandler.timePeriod.THIS_WEEK;
                        initLineChart();
                        mCubicValueLineChart.startAnimation();
                        break;
                    case 2:
                        timePeriod = HistoryHandler.timePeriod.THIS_MONTH;
                        initLineChart();
                        mCubicValueLineChart.startAnimation();
                        break;
                    case 3:
                        timePeriod = HistoryHandler.timePeriod.THIS_YEAR;
                        initLineChart();
                        mCubicValueLineChart.startAnimation();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initCharts() {

        initTodayEarned();
        initTotalEarned();
        initPieChart();
        initBarChart();
        initLineChart();
    }

    private void initTodayEarned() {

        today_earned_hour.setText(historyHandler.getTodayTimeInHour());
        today_earned_min.setText(historyHandler.getTodayTimeInMin(true));
    }

    private void initTotalEarned() {

        total_earned_time_tv.setText(historyHandler.getTotalTimeInShort(false));
        total_earned_unit_tv.setText(historyHandler.getTotalTimeInShort(true));
    }

    private void initPieChart() {

        String percentage = String.valueOf((long)(Double.longBitsToDouble(completedTime) /
                Double.longBitsToDouble(totalTime) * 100)) + getString(R.string.percentage_sign);
        goal_percentage_tv.setText(percentage);
        mPieChart = (PieChart) view.findViewById(R.id.pieChart);
        mPieChart.clearChart();
        mPieChart.addPieSlice(new PieModel("Completed", completedTime, accent));
        mPieChart.addPieSlice(new PieModel("Incomplete", totalTime - completedTime, primaryDark));
        mPieChart.setAnimationTime(500);
    }

    private void initBarChart() {

        mBarChart = (BarChart) view.findViewById(R.id.barChart);
        mBarChart.clearChart();
        long[] dataWithInThisWeek = historyHandler.getMinutesWithinThisWeek();

        for (int i = 0; i < 7; i++)
            if (i == 6)
                mBarChart.addBar(new BarModel(dataWithInThisWeek[i], primary));
            else
                mBarChart.addBar(new BarModel(dataWithInThisWeek[i], white));

        mBarChart.setShowValues(true);
        mBarChart.setDrawingCacheBackgroundColor(white);
        mBarChart.setLegendColor(white);
        mBarChart.setAnimationTime(500);
    }

    private void initLineChart() {

        mCubicValueLineChart = (ValueLineChart) view.findViewById(R.id.cubicLineChart);
        mCubicValueLineChart.clearChart();
        ValueLineSeries series = new ValueLineSeries();

        switch (timePeriod) {
            case TODAY:
                ArrayList<Float> dataOfToday = historyHandler.getDataWithin(HistoryHandler.timePeriod.TODAY);
                for (int i = 0; i <= 23; i++)
                    series.addPoint(new ValueLinePoint(legendInNum[i], dataOfToday.get(i)));
                break;
            case THIS_WEEK:
                ArrayList<Float> dataOfThisWeek = historyHandler.getDataWithin(HistoryHandler.timePeriod.THIS_WEEK);
                for (int i = 0; i <= 6; i++)
                    series.addPoint(new ValueLinePoint(legendForWeek[i], dataOfThisWeek.get(i)));
                break;
            case THIS_MONTH:
                ArrayList<Float> dataOfThisMonth = historyHandler.getDataWithin(HistoryHandler.timePeriod.THIS_MONTH);
                for (int i = 0; i <= historyHandler.howManyDaysInThisMonth() - 1; i++)
                    series.addPoint(new ValueLinePoint(legendInNum[i], dataOfThisMonth.get(i)));
                break;
            case THIS_YEAR:
                ArrayList<Float> dataOfThisYear = historyHandler.getDataWithin(HistoryHandler.timePeriod.THIS_YEAR);
                for (int i = 0; i <= 11; i++)
                    series.addPoint(new ValueLinePoint(legendInNum[i], dataOfThisYear.get(i)));
                break;
            default:
                break;
        }

        series.setColor(accent);
        mCubicValueLineChart.setShowDecimal(true);
        mCubicValueLineChart.addSeries(series);
        mCubicValueLineChart.setAnimationTime(500);
        mCubicValueLineChart.setAxisTextColor(white);
        mCubicValueLineChart.setLegendColor(white);
    }

    private void gotoDailyGoalDetail() {

        if (detailFragment == null)
            detailFragment = new DetailFragment();
        detailFragment.setSharedElementEnterTransition(new DetailsTransition());
        detailFragment.setSharedElementReturnTransition(new DetailsTransition());


        getActivity().getFragmentManager()
                .beginTransaction()
                .addSharedElement(goal_card_view, getString(R.string.transition_name_goal_view))
                .replace(R.id.content_view, detailFragment)
                .addToBackStack(null)
                .commit();

        MainActivity.of(getActivity()).setPresentFragment(1);
        MainActivity.of(getActivity()).animateHomeIcon(MaterialMenuDrawable.IconState.ARROW, false);
        MainActivity.of(getActivity()).setToolBarElevation(0);
    }

    private void setCardsInvisible() {

        goal_percentage_tv.setVisibility(View.INVISIBLE);
        mPieChart.setVisibility(View.INVISIBLE);
        mBarChart.setVisibility(View.INVISIBLE);
        mCubicValueLineChart.setVisibility(View.INVISIBLE);
        card_view_today.setVisibility(View.INVISIBLE);
        goal_card_view.setVisibility(View.INVISIBLE);
        total_card_view.setVisibility(View.INVISIBLE);
        bar_chart_card_view.setVisibility(View.INVISIBLE);
        overview_card_view.setVisibility(View.INVISIBLE);
    }

    @TargetApi(21)
    public void animateRevealShow(final View viewRoot, int delay, final int includedAnimator) {

        int cx = (int) viewRoot.getTranslationX() + 1;
        int cy = (int) viewRoot.getTranslationY() + 1;
        int radius = viewRoot.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, radius);
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                viewRoot.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                switch (includedAnimator) {

                    case R.id.pieChart:
                        mPieChart.setVisibility(View.VISIBLE);
                        mPieChart.startAnimation();
                        goal_percentage_tv.setVisibility(View.VISIBLE);
                        goal_percentage_tv.startAnimation(daily_goal_title_fade_in);
                        break;

                    case R.id.barChart:
                        mBarChart.setVisibility(View.VISIBLE);
                        mBarChart.startAnimation();
                        break;

                    case R.id.cubicLineChart:
                        mCubicValueLineChart.setVisibility(View.VISIBLE);
                        mCubicValueLineChart.startAnimation();
                        break;

                    default:
                        break;
                }
            }
        });
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(500);
        anim.setStartDelay(delay);
        anim.start();
    }

}