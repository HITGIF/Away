/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.support.v7.app.AlertDialog;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import carbonylgroup.away.R;
import carbonylgroup.away.classes.HistoryHandler;


public class OnTimeActivity extends Activity {

    private int time_seconds;
    private int time_minutes;
    private int time_hours;
    private int timeInSeconds;
    private int targetTime;
    private String timeInText;
    private TimerTask timerTask;
    private HistoryHandler historyHandler;
    private PowerManager.WakeLock wakeLock = null;

    private View timing_layout;
    private Timer timer;
    private Button giveUp_bt;
    private TextView time_text;
    private TextView intro;
    private TextView timeDialog_title;
    private TextView timeDialog_msg;
    private TextView timeDialog_timeText;
    private ImageView timeDialog_markImage;
    private AlertDialog.Builder giveUpDialogBuilder;
    private AlertDialog.Builder lostDialogBuilder;
    private AlertDialog.Builder winDialogBuilder;
    private View.OnClickListener giveUp_btOC;
    private DialogInterface.OnClickListener giveUpOC;

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

                    /** Completed */
                    if (timeInSeconds == 0) {
                        timeUp();
                        historyHandler.writeHistory(new Date(), targetTime);
                        showWinDialog();
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

        initOnClick();
        initValue();
        initUI();

        startTimerTask();
    }

    @Override
    protected void onResume() {

        super.onResume();
        ViewTreeObserver vto2 = timing_layout.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                timing_layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    animateRevealShow(timing_layout);
            }
        });
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            animateRevealGone(timing_layout);
    }

    private void initOnClick() {

        giveUp_btOC = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGiveUpDialog();

            }
        };
        giveUpOC = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                endActivity();
            }
        };
    }

    private void initValue() {

        historyHandler = new HistoryHandler(OnTimeActivity.this);
        
        initView();

        /** Get Intent Value */
        Intent intent = getIntent();
        targetTime = intent.getIntExtra("time", 0);
        //initTimeWithS(targetTime);
        initTimeWithS(5);
        if (intent.getIntExtra("time", 0) != 0) {
            timeInText = getTimeInHMS();
            showTime(timeInText);
        }
    }

    private void initView() {

        timing_layout = findViewById(R.id.timing_layout);

        LayoutInflater inflater = getLayoutInflater();
        View timeDialogView = inflater.inflate(R.layout.time_dialog_view, (ViewGroup) findViewById(R.id.dialog_rootView));

        time_text = (TextView) findViewById(R.id.time_show);
        giveUp_bt = (Button) findViewById(R.id.giveup_bt);
        intro = (TextView) findViewById(R.id.intro);

        giveUp_bt.setOnClickListener(giveUp_btOC);

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
        giveUpDialogBuilder = new AlertDialog.Builder(OnTimeActivity.this);
        giveUpDialogBuilder.setTitle(getString(R.string.giveUpDiaTitle));
        giveUpDialogBuilder.setMessage(getString(R.string.giveUpDiaMessage));
        giveUpDialogBuilder.setNegativeButton(getString(R.string.giveUp), giveUpOC);
        giveUpDialogBuilder.setPositiveButton(getString(R.string.stay), null);

        timeDialog_title = (TextView) timeDialogView.findViewById(R.id.timeDialog_title);
        timeDialog_msg = (TextView) timeDialogView.findViewById(R.id.timeDialog_msg);
        timeDialog_timeText = (TextView) timeDialogView.findViewById(R.id.timeDialog_timeText);
        timeDialog_markImage = (ImageView) timeDialogView.findViewById(R.id.timeDialog_markImage);

        lostDialogBuilder = new AlertDialog.Builder(OnTimeActivity.this);
        lostDialogBuilder.setPositiveButton(getString(R.string.myFault), giveUpOC);
        lostDialogBuilder.setView(timeDialogView);
        lostDialogBuilder.setCancelable(false);

        winDialogBuilder = new AlertDialog.Builder(OnTimeActivity.this);
        winDialogBuilder.setPositiveButton(getString(R.string.OK), giveUpOC);
        winDialogBuilder.setView(timeDialogView);
        winDialogBuilder.setCancelable(false);
    }

    private void initUI() {

        initTypeFace();
        fullScreen(true);
    }

    private void initTypeFace() {

        Typeface futura_lt_light = Typeface.createFromAsset(getAssets(), "fonts/futura_lt_light.ttf");
        Typeface futura_lt_medium = Typeface.createFromAsset(getAssets(), "fonts/futura_lt_medium.ttf");

        time_text.setTypeface(futura_lt_light);
        giveUp_bt.setTypeface(futura_lt_medium);
        intro.setTypeface(futura_lt_medium);
        timeDialog_timeText.setTypeface(futura_lt_light);
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

        giveUpDialogBuilder.show();
    }

    private void showWinDialog() {

        timeDialog_title.setText(getString(R.string.goodJob));
        timeDialog_msg.setText(getString(R.string.targetAchieved));
        timeDialog_timeText.setText(getTimeInHMS(targetTime));
        timeDialog_timeText.setTextColor(getResources().getColor(R.color.light_green));
        timeDialog_markImage.setImageResource(R.drawable.check_mark);

        winDialogBuilder.show();
    }

    private void showLostDialog() {

        timeDialog_title.setText(getString(R.string.whatAPity));
        timeDialog_msg.setText(getString(R.string.targetNotAchieved));
        timeDialog_timeText.setText(getTimeInHMS(targetTime));
        timeDialog_timeText.setTextColor(getResources().getColor(R.color.light_orange));
        timeDialog_markImage.setImageResource(R.drawable.cross_mark);

        lostDialogBuilder.show();
    }

    public static int dip2px(Context context, float dipValue) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @TargetApi(21)
    public void animateRevealShow(View viewRoot) {

        int cx = viewRoot.getWidth() / 2;
        int cy = viewRoot.getHeight() - dip2px(OnTimeActivity.this, 60);
        int radius = viewRoot.getHeight();

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, radius);
        viewRoot.setVisibility(View.VISIBLE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(500);
        anim.start();
    }

    @TargetApi(21)
    public void animateRevealGone(final View viewRoot) {

        int cx = viewRoot.getWidth() / 2;
        int cy = viewRoot.getHeight() - dip2px(OnTimeActivity.this, 60);
        int radius = viewRoot.getHeight();

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, radius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                viewRoot.setVisibility(View.INVISIBLE);
                OnTimeActivity.this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(400);
        anim.start();
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

    private void acquireWakeLock() {

        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {

        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }

//                    /** Prepare blurred bitmap */
//                    if (timeInSeconds == 2) {
//                        showTime(getTimeInHMS(0));
//                        shot_screen = ScreenShot(OnTimeActivity.this);
//                        showTime(getTimeInHMS(2));
//                        Thread thread = new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                shot_screen = FastBlur.doBlur(shot_screen, 10, false);
//                            }
//                        });
//                        thread.start();
//                    }

//        public Bitmap ScreenShot(Activity activity) {
//
//        View view = activity.getWindow().getDecorView();
//        view.buildDrawingCache();
//        Display display = activity.getWindowManager().getDefaultDisplay();
//
//        int widths = display.getWidth();
//        int heights = display.getHeight();
//        view.setDrawingCacheEnabled(true);
//        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, widths, heights);
//        view.destroyDrawingCache();
//
//        return bmp;
//    }
}
