/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.classes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import carbonylgroup.away.R;


public class HistoryHandler {

    public enum timePeriod {TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR}

    private enum getTimeAsInput {YEAR, MONTH, DAY, HOUR, MIN}

    private long totalTime;
    private long goalTime;
    private int themeIdNow;
    private Locale languageIdNow;

    private int[] themeIds = {R.style.Theme_Tomato, R.style.Theme_Tangerine,
            R.style.Theme_Basil, R.style.Theme_Sage, R.style.Theme_Peacock,
            R.style.Theme_Blueberry, R.style.Theme_Lavender, R.style.Theme_Grape,
            R.style.Theme_Flamingo, R.style.Theme_Graphite};

    private int[] colorTitles = {R.string.tomato, R.string.tangerine,
            R.string.basil, R.string.sage, R.string.peacock,
            R.string.blueberry, R.string.lavender, R.string.grape,
            R.string.flamingo, R.string.graphite};

    private int[] colorImgs = {R.drawable.color_img_tomato, R.drawable.color_img_tangerine,
            R.drawable.color_img_basil, R.drawable.color_img_sage, R.drawable.color_img_peacock,
            R.drawable.color_img_bluebarry, R.drawable.color_img_lavender, R.drawable.color_img_grape,
            R.drawable.color_img_flamingo, R.drawable.color_img_graphite};

    private Locale[] languageIds = {Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE,
            Locale.TRADITIONAL_CHINESE, Locale.GERMAN, Locale.JAPANESE};

    private int[] languageTitles = {R.string.english, R.string.simplified_chinese,
            R.string.traditional_chinese, R.string.german, R.string.japanese};

    private int[] languageImgs = {R.drawable.uk, R.drawable.china,
            R.drawable.taiwan, R.drawable.germany, R.drawable.japan};

    private Context mContext;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS", Locale.CHINA);
    private SharedPreferences spReader;
    private ArrayList<History> all_Histories;

    public HistoryHandler(Context context) {

        mContext = context;
        readHistory();
    }

    /* For Line Chart */
    public int howManyDaysInThisMonth() {
        Calendar calendar = Calendar.getInstance();
        switch (calendar.MONTH) {
            case 4:
                return 30;
            case 6:
                return 30;
            case 9:
                return 30;
            case 11:
                return 30;
            default:
                return 31;
        }
    }

    public ArrayList<Float> getDataWithin(timePeriod period) {

        ArrayList<Float> time_num = new ArrayList<>();
        switch (period) {

            case TODAY:
                time_num = getDataWithinToday();
                break;
            case THIS_WEEK:
                time_num = getDataWithinThisWeek();
                break;
            case THIS_MONTH:
                time_num = getDataWithinThisMonth();
                break;
            case THIS_YEAR:
                time_num = getDataWithinThisYear();
                break;
        }
        return time_num;
    }

    private ArrayList<Float> getDataWithinToday() {

        ArrayList<History> historiesOfToday = new ArrayList<>();
        Calendar calendar;

        for (History h : all_Histories) {
            calendar = Calendar.getInstance();
            if (isTheSameDay(h.date, calendar.getTime())) historiesOfToday.add(h);
        }

        ArrayList<Float> dataOfToday = new ArrayList<>();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        float wholeTime = 0;

        for (int i = 0; i <= 23; i++) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            for (History h : historiesOfToday) {
                if (isTheSameHour(calendar.getTime(), h.date))
                    wholeTime += (float) (Math.round(h.time_took / 36)) / 100;
            }
            dataOfToday.add(wholeTime);
        }

        return dataOfToday;
    }

    private ArrayList<Float> getDataWithinThisWeek() {

        ArrayList<History> historiesOfThisWeek = new ArrayList<>();
        Calendar calendar;

        for (History h : all_Histories) {
            calendar = Calendar.getInstance();
            if (isTheSameWeek(h.date, calendar.getTime())) historiesOfThisWeek.add(h);
        }

        ArrayList<Float> dataOfThisWeek = new ArrayList<>();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, 1);
        float wholeTime = 0;

        for (int i = 0; i <= 6; i++) {
            for (History h : historiesOfThisWeek) {
                if (isTheSameDay(calendar.getTime(), h.date))
                    wholeTime += (float) (Math.round(h.time_took / 36)) / 100;
            }
            dataOfThisWeek.add(wholeTime);
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        return dataOfThisWeek;
    }

    private ArrayList<Float> getDataWithinThisMonth() {

        ArrayList<History> historiesOfThisMonth = new ArrayList<>();
        Calendar calendar;

        for (History h : all_Histories) {
            calendar = Calendar.getInstance();
            if (isTheSameMonth(h.date, calendar.getTime())) historiesOfThisMonth.add(h);
        }

        ArrayList<Float> dataOfThisMonth = new ArrayList<>();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        float wholeTime = 0;

        for (int i = 0; i <= howManyDaysInThisMonth() - 1; i++) {
            for (History h : historiesOfThisMonth) {
                if (isTheSameDay(calendar.getTime(), h.date))
                    wholeTime += (float) (Math.round(h.time_took / 36)) / 100;
            }
            dataOfThisMonth.add(wholeTime);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dataOfThisMonth;
    }

    private ArrayList<Float> getDataWithinThisYear() {

        ArrayList<History> historiesOfThisYear = new ArrayList<>();
        Calendar calendar;

        for (History h : all_Histories) {
            calendar = Calendar.getInstance();
            if (isTheSameYear(h.date, calendar.getTime())) historiesOfThisYear.add(h);
        }

        ArrayList<Float> dataOfThisYear = new ArrayList<>();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, 0);
        float wholeTime = 0;

        for (int i = 0; i <= 11; i++) {
            for (History h : historiesOfThisYear) {
                if (isTheSameMonth(calendar.getTime(), h.date))
                    wholeTime += (float) (Math.round(h.time_took / 36)) / 100;
            }
            dataOfThisYear.add(wholeTime);
            calendar.add(Calendar.MONTH, 1);
        }

        return dataOfThisYear;
    }

    private boolean isTheSameHour(Date date1, Date date2) {

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);

        return (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
                && calendar1.get(Calendar.HOUR_OF_DAY) == calendar2.get(Calendar.HOUR_OF_DAY));
    }

    private boolean isTheSameDay(Date date1, Date date2) {

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);

        return (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR));
    }

    private boolean isTheSameWeek(Date date1, Date date2) {

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);

        return (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR));
    }

    private boolean isTheSameMonth(Date date1, Date date2) {

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);

        return (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH));
    }

    private boolean isTheSameYear(Date date1, Date date2) {

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);

        return (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR));
    }

    /* For Bar Chart */
    public long[] getMinutesWithinThisWeek() {

        long[] dataOfThisWeek = {0, 0, 0, 0, 0, 0, 0};
        int nowProcessing = 0;
        Calendar calendar;

        for (int i = 6; i >= 0; i--) {

            calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -i);
            dataOfThisWeek[nowProcessing] = getTimeOfDay(calendar.getTime()) / 60;
            nowProcessing++;
        }

        return dataOfThisWeek;
    }

    /* For TextView */
    public long getTimeOfDay(Date date) {

        int dayTime = 0;
        for (History h : all_Histories) {
            if (isTheSameDay(h.date, date)) {
                dayTime += h.time_took;
            }
        }
        return dayTime;
    }

    public String getTodayTimeInHour() {

        long hour = getTimeOfDay(new Date()) / 3600;
        return hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour);
    }

    public String getTodayTimeInMin(Boolean withUnit) {

        long minute = getTimeOfDay(new Date()) % 3600 / 60;
        String minutes = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
        return withUnit ? getResStr(R.string.hour_short) + " " + minutes + getResStr(R.string.min_short) : minutes;
    }

    public String getTotalTimeInShort(boolean forUnit) {

        String time_display;
        String unit;

        if (totalTime < 31536000) {
            if (totalTime < 2628000) {
                if (totalTime < 86400) {
                    if (totalTime < 3600) {
                        /**Get As Minute**/
                        time_display = getTimeAs(totalTime, getTimeAsInput.MIN);
                        unit = getResStr(R.string.min_short);
                    } else {
                        /**Get As Hour**/
                        time_display = getTimeAs(totalTime, getTimeAsInput.HOUR);
                        unit = getResStr(R.string.hour_short);
                    }
                } else {
                    /**Get As Day**/
                    time_display = getTimeAs(totalTime, getTimeAsInput.DAY);
                    unit = getResStr(R.string.day_short);
                }
            } else {
                /**Get As Month**/
                time_display = getTimeAs(totalTime, getTimeAsInput.MONTH);
                unit = getResStr(R.string.month_short);
            }
        } else {
            /**Get As Year**/
            time_display = getTimeAs(totalTime, getTimeAsInput.YEAR);
            unit = getResStr(R.string.year_short);
        }

        return forUnit ? unit : time_display;
    }

    /* For Goal */
    public void setGoal(long _hour, long _minute) {

        long _goalTime = HM2S(_hour, _minute);
        if (_goalTime != goalTime)
            writeGoal(_goalTime);
    }

    public long getGoalInNum(int fullOrHOrS) {

        readGoal();
        switch (fullOrHOrS) {
            case 0:
                return goalTime;
            case 1:
                return goalTime / 3600;
            case 2:
                return goalTime % 3600 / 60;
        }
        return 0;
    }

    public String getGoalInStr() {

        readGoal();
        return S2HM(goalTime);
    }

    public String S2HM(long _second) {

        String hour = (_second / 3600) < 10 ? "0" + String.valueOf(_second / 3600) : String.valueOf(_second / 3600);
        String minute = (_second % 3600 / 60) < 10 ? "0" + String.valueOf(_second % 3600 / 60) : String.valueOf(_second % 3600 / 60);
        return hour + ":" + minute;
    }

    private long HM2S(long _hour, long _minute) {

        return _hour * 3600 + _minute * 60;
    }

    /* For Theme */
    public int getThemeIdNow() {

        readTheme();
        return themeIdNow;
    }

    public void setThemeIdNow(int _themeIdPos) {

        if (themeIds[_themeIdPos] != themeIdNow)
            writeTheme(_themeIdPos);
    }

    public List<Map<String, Object>> getThemeData() {

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;

        for (int i = 0; i < themeIds.length; i++) {

            map = new HashMap<>();
            map.put("img", colorImgs[i]);
            if (themeIdNow == themeIds[i]) {
                map.put("selectedTitle", getResStr(colorTitles[i]));
                map.put("checked", R.drawable.ic_done_blue_48dp);
            } else {
                map.put("title", getResStr(colorTitles[i]));
                map.put("cover", R.drawable.color_img_white);
            }
            list.add(map);
        }

        return list;
    }

    /* For Language */
    public Locale getLanguageIdNow() {

        readLanguage();
        return languageIdNow;
    }

    public void setLanguageIdNow(int _languageIdPos) {

        if (languageIds[_languageIdPos] != languageIdNow)
            writeLanguage(_languageIdPos);
    }

    public List<Map<String, Object>> getLanguageData() {

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;

        for (int i = 0; i < languageIds.length; i++) {

            map = new HashMap<>();
            map.put("img", languageImgs[i]);
            if (languageIdNow.toLanguageTag().equals(languageIds[i].toLanguageTag())) {
                map.put("selectedTitle", getResStr(languageTitles[i]));
                map.put("checked", R.drawable.ic_done_blue_48dp);
            } else {
                map.put("title", getResStr(languageTitles[i]));
            }
            Log.d("[][]", "getLanguageData: "+languageIdNow.toLanguageTag());
            Log.d("[][]", "getLanguageData: "+languageIds[i].toLanguageTag());
            list.add(map);
        }

        return list;
    }

    /* IO */
    public void writeHistory(Date _date, int _time_took) {

        all_Histories.add(new History(_date, _time_took));
        totalTime += _time_took;

        String hs = "";
        for (History tem : all_Histories) {
            hs += sdf.format(tem.date) + "<#>" + String.valueOf(tem.time_took) + "<@>";
        }
        hs = hs.substring(0, hs.length() - 3);

        spReader = mContext.getSharedPreferences("data", Activity.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = spReader.edit();
        spEditor.putString("histories", hs);
        spEditor.putLong("totalTime", totalTime);
        spEditor.apply();
    }

    private void readHistory() {

        spReader = mContext.getSharedPreferences("data", Activity.MODE_PRIVATE);
        all_Histories = new ArrayList<>();
        totalTime = spReader.getLong("totalTime", 0);
        String h = spReader.getString("histories", null);
        if (h != null) {
            try {
                String[] hs = h.split("<@>");
                for (String tem : hs) {
                    String[] history = tem.split("<#>");
                    all_Histories.add(new History(sdf.parse(history[0]), Long.parseLong(history[1])));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeGoal(long _goalTime) {

        spReader = mContext.getSharedPreferences("data", Activity.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = spReader.edit();
        goalTime = _goalTime;
        spEditor.putLong("goal", goalTime);
        spEditor.apply();
    }

    private void readGoal() {

        spReader = mContext.getSharedPreferences("data", Activity.MODE_PRIVATE);
        goalTime = spReader.getLong("goal", 0);
    }

    private void writeTheme(int _themeId) {

        spReader = mContext.getSharedPreferences("data", Activity.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = spReader.edit();
        themeIdNow = themeIds[_themeId];
        spEditor.putInt("theme", themeIdNow);
        spEditor.apply();
    }

    private void readTheme() {

        spReader = mContext.getSharedPreferences("data", Activity.MODE_PRIVATE);
        themeIdNow = spReader.getInt("theme", themeIds[2]);
    }

    private void writeLanguage(int _languageIdPos) {

        spReader = mContext.getSharedPreferences("data", Activity.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = spReader.edit();
        languageIdNow = languageIds[_languageIdPos];
        spEditor.putString("language", languageIdNow.toLanguageTag());
        spEditor.apply();
    }

    private void readLanguage() {

        spReader = mContext.getSharedPreferences("data", Activity.MODE_PRIVATE);
        languageIdNow = Locale.forLanguageTag(spReader.getString("language", languageIds[0].toLanguageTag()));
    }

    private String getTimeAs(long time, getTimeAsInput input) {

        long time_num;
        String time_str;
        switch (input) {

            case YEAR:
                time_num = time / 31536000;
                break;
            case MONTH:
                time_num = time / 2628000;
                break;
            case DAY:
                time_num = time / 86400;
                break;
            case HOUR:
                time_num = time / 3600;
                break;
            case MIN:
                time_num = time / 60;
                break;
            default:
                time_num = 0;
                break;
        }

        time_str = time_num < 10 ? "0" + String.valueOf(time_num) : String.valueOf(time_num);
        return time_str;
    }

    @NonNull
    private String getResStr(int id) {

        return mContext.getResources().getString(id);
    }

}
