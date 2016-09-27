/** Copyright (C) 2016 Gustav Wang */

package carbonylgroup.away.activities;

import android.app.Activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import carbonylgroup.away.R;
import carbonylgroup.away.classes.FastBlur;
import carbonylgroup.away.classes.History;
import carbonylgroup.away.material_design_uilibrary.widgets.Dialog;
import carbonylgroup.away.material_design_uilibrary.widgets.TimeDialog;

public class OnTimeActivity extends Activity {

    private int time_seconds;
    private int time_minutes;
    private int time_hours;
    private int timeInSeconds;
    private int targetTime;
    private long totalTime;

    private ArrayList<History> all_Histories;
    private String timeInText;
    private Animation giveUp_oc;
    private Animation giveUp_or;
    private TextView time_text;
    private TextView giveUp_bt;
    private TextView intro;
    private Timer timer;
    private TimerTask timerTask;
    private Bitmap shot_screen;
    private Date date_now;

    SharedPreferences spReader;
    SharedPreferences.Editor spEditor;
    SimpleDateFormat sdf;

    Dialog giveUpDialog;
    TimeDialog timedialog;
    TimeDialog lostDialog;

    PowerManager.WakeLock wakeLock = null;


    /**
     * Timer_Process
     */
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    /** Keep working when sleeping */
                    acquireWakeLock();

                    initTimeWithS(timeInSeconds - 1);
                    timeInText = getTimeInHMS();
                    showTime(timeInText);

                    /** Prepare blurred bitmap */
                    if (timeInSeconds == 2) {
                        showTime(getTimeInHMS(0));
                        shot_screen = ScreenShot(OnTimeActivity.this);
                        showTime(getTimeInHMS(2));
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                shot_screen = FastBlur.doBlur(shot_screen, 10, false);
                            }
                        });
                        thread.start();
                    }
                    /** Completed */
                    if (timeInSeconds == 0) {
                        timeUp();
                        writeData(date_now, targetTime);
                        showTimeUpDialog();
                    }
                    /** Lost */
                    if (isBackground(OnTimeActivity.this)) {
                        timeUp();
                        showLostDialog();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.on_timing);

        initValue();
        initUI();
        initAnim();
        initOnClick();

        startTimerTask();

        readData();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        releaseWakeLock();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showGiveUpDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void endActivity() {

        timeUp();
        OnTimeActivity.this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initValue() {

        time_text = (TextView) findViewById(R.id.time_show);
        giveUp_bt = (TextView) findViewById(R.id.giveup_bt);
        intro = (TextView) findViewById(R.id.intro);

        spReader = getSharedPreferences("data", Activity.MODE_PRIVATE);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS", Locale.CHINA);
        date_now = new Date();

        /** Get Intent Value */
        Intent intent = getIntent();
        targetTime = intent.getIntExtra("time", 0);
        initTimeWithS(targetTime);
        //initTimeWithS(5);
        if (intent.getIntExtra("time", 0) != 0) {

            timeInText = getTimeInHMS();
            showTime(timeInText);
        }

        /** Timer */
        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        };

        /** Dialogs */
        giveUpDialog = new Dialog(OnTimeActivity.this, getString(R.string.giveUpDiaTitle), getString(R.string.giveUpDiaMessage), getString(R.string.stay));
        giveUpDialog.addCancelButton(getString(R.string.giveUp));
        giveUpDialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endActivity();
            }
        });

        timedialog = new TimeDialog(OnTimeActivity.this, getString(R.string.goodJob), getString(R.string.targetAchieved), getString(R.string.OK), getTimeInHMS(targetTime), true, shot_screen, false);
    }

    private void initUI() {

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/futura_lt_light.ttf");

        time_text.setTypeface(typeFace);
        giveUp_bt.setTypeface(typeFace);
        intro.setTypeface(typeFace);
        ((Button) findViewById(R.id.back_giveup)).setTypeface(typeFace);

        fullScreen(true);
    }

    private void initAnim() {

        giveUp_oc = AnimationUtils.loadAnimation(this, R.anim.start_button_onclick);
        giveUp_oc.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                giveUp_bt.setAlpha(0);
            }
        });

        giveUp_or = AnimationUtils.loadAnimation(this, R.anim.start_button_onrelease);
        giveUp_or.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                giveUp_bt.setAlpha(1);
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

        giveUp_bt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        giveUp_bt.startAnimation(giveUp_oc);
                        break;
                    case MotionEvent.ACTION_UP:
                        giveUp_bt.startAnimation(giveUp_or);
                        showGiveUpDialog();
                        break;
                }
                return false;
            }
        });
    }

    private void initTimeWithS(int seconds_h) {

        timeInSeconds = seconds_h;
        time_hours = seconds_h / 3600;
        time_minutes = seconds_h % 3600 / 60;
        time_seconds = (seconds_h % 3600) % 60;
    }

    private String getTimeInHMS() {

        String hour = time_hours < 10 ? "0" + String.valueOf(time_hours) : String.valueOf(time_hours);
        String minute = time_minutes < 10 ? "0" + String.valueOf(time_minutes) : String.valueOf(time_minutes);
        String second = time_seconds < 10 ? "0" + String.valueOf(time_seconds) : String.valueOf(time_seconds);
        return hour + ":" + minute + ":" + second;
    }

    private String getTimeInHMS(long time) {

        long hour = time / 3600;
        long minute = time % 3600 / 60;
        long second = (time % 3600) % 60;
        String hours = hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour);
        String minutes = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
        String seconds = second < 10 ? "0" + String.valueOf(second) : String.valueOf(second);
        return hours + ":" + minutes + ":" + seconds;
    }

    private void showTime(String timeInText) {

        time_text.setText(timeInText);
    }

    private void timeUp() {

        endTimerTask();
    }

    private void startTimerTask() {

        timer.schedule(timerTask, 1000, 1000);
    }

    private void endTimerTask() {

        timer.cancel();
    }

    private void showGiveUpDialog() {

        if (!timedialog.isShowing())
            giveUpDialog.show();
    }

    private void showTimeUpDialog() {

        timedialog = new TimeDialog(OnTimeActivity.this, getString(R.string.goodJob), getString(R.string.targetAchieved), getString(R.string.OK), getTimeInHMS(targetTime), true, shot_screen, false);
        timedialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endActivity();
            }
        });
        if (giveUpDialog.isShowing())
            giveUpDialog.dismiss();
        timedialog.show();
    }

    private void showLostDialog() {

        lostDialog = new TimeDialog(OnTimeActivity.this, getString(R.string.whatAPity), getString(R.string.targetNotAchieved), getString(R.string.myFault), getTimeInHMS(targetTime), false, ScreenShot(OnTimeActivity.this), true);
        lostDialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endActivity();
            }
        });
        if (giveUpDialog.isShowing())
            giveUpDialog.dismiss();
        lostDialog.show();
    }

    private void readData() {

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

    private void writeData(Date _date, int _time_took) {

        all_Histories.add(new History(_date, _time_took));
        totalTime += _time_took;

        String hs = "";
        for (History tem : all_Histories) {
            hs += sdf.format(date_now) + "<#>" + String.valueOf(tem.time_took) + "<@>";
        }
        hs = hs.substring(0, hs.length() - 3);

        spEditor = spReader.edit();
        spEditor.putString("histories", hs);
        spEditor.putLong("totalTime", totalTime);
        spEditor.apply();
    }

    private void fullScreen(boolean enable) {

        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public Bitmap ScreenShot(Activity activity) {

        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();
        Display display = activity.getWindowManager().getDefaultDisplay();

        int widths = display.getWidth();
        int heights = display.getHeight();
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, widths, heights);
        view.destroyDrawingCache();

        return bmp;
    }

    private static boolean isBackground(final Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }

}
