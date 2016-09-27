package carbonylgroup.away.material_design_uilibrary.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import carbonylgroup.away.R;
import carbonylgroup.away.material_design_uilibrary.views.ButtonFlat;
import carbonylgroup.away.classes.FastBlur;


public class TimeDialog extends android.app.Dialog {

    Context context;
    View view;
    View backView;
    ImageView backrootView;
    String message;
    String accept_message;
    TextView messageTextView;
    String title;
    String time_intext;
    TextView titleTextView;
    TextView timeTextView;
    ImageView markImageView;
    Bitmap screenback;

    boolean good;
    boolean blurornot;

    ButtonFlat buttonAccept;
    ButtonFlat buttonCancel;

    String buttonCancelText;

    View.OnClickListener onAcceptButtonClickListener;
    View.OnClickListener onCancelButtonClickListener;

    FastBlur blur;
    Resources resources;


    public TimeDialog(Context context, String title, String message, String accept_message, String time_tx, boolean good, Bitmap scb, boolean blurornot) {
        super(context, android.R.style.Theme_Translucent);
        this.context = context;// init Context
        this.message = message;
        this.title = title;
        this.accept_message = accept_message;
        this.time_intext = time_tx;
        this.good = good;
        this.screenback = scb;
        this.blurornot = blurornot;
    }

    public void addCancelButton(String buttonCancelText) {
        this.buttonCancelText = buttonCancelText;
    }

    public void addCancelButton(String buttonCancelText, View.OnClickListener onCancelButtonClickListener) {
        this.buttonCancelText = buttonCancelText;
        this.onCancelButtonClickListener = onCancelButtonClickListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_dialog_view);

        fullScreen(true);

        view = (RelativeLayout) findViewById(R.id.contentDialog);
        backView = (RelativeLayout) findViewById(R.id.dialog_rootView);
        backrootView = (ImageView) findViewById(R.id.dialog_backrootView);

        if(blurornot)
            backrootView.setImageBitmap(blur.doBlur(screenback,10, false));
        else
            backrootView.setImageBitmap(screenback);

        Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/futura_lt_light.ttf");

        this.titleTextView = (TextView) findViewById(R.id.title);
        this.titleTextView.setTypeface(typeFace);
        //titleTextView.setTextColor(Color.WHITE);
        setTitle(title);

        this.messageTextView = (TextView) findViewById(R.id.msg);
        this.messageTextView.setTypeface(typeFace);
        messageTextView.setTextColor(Color.WHITE);
        setMessage(message);

        this.timeTextView = (TextView) findViewById(R.id.time);
        this.timeTextView.setTypeface(typeFace);
        if (this.good)
            this.timeTextView.setTextColor(context.getResources().getColor(R.color.light_green));
        else
            this.timeTextView.setTextColor(context.getResources().getColor(R.color.light_orange));
        setTime(time_intext);

        this.markImageView = (ImageView) findViewById(R.id.markImage);

        resources = context.getResources();
        if (this.good)
            this.markImageView.setImageResource(R.drawable.check_mark);

        else
            this.markImageView.setImageResource(R.drawable.cross_mark);

        this.buttonAccept = (ButtonFlat) findViewById(R.id.button_accept);
        this.buttonAccept.setText(accept_message);
        //   buttonAccept.setTextColor(Color.WHITE);
        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onAcceptButtonClickListener != null)
                    onAcceptButtonClickListener.onClick(v);
            }
        });

        if (buttonCancelText != null) {
            this.buttonCancel = (ButtonFlat) findViewById(R.id.button_cancel);
            this.buttonCancel.setVisibility(View.VISIBLE);
            this.buttonCancel.setText(buttonCancelText);
//			this.buttonCancel.setTextColor(Color.WHITE);
            buttonCancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    if (onCancelButtonClickListener != null)
                        onCancelButtonClickListener.onClick(v);
                }
            });
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    @Override
    public void show() {
        // TODO 自动生成的方法存根
        super.show();
        // set dialog enter animations
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_main_show_amination));
        backView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.dialog_root_show_amin));
    }

    // GETERS & SETTERS

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        messageTextView.setText(message);
    }

    public void setTime(String time) {
        this.time_intext = time;
        timeTextView.setText(time);
    }

    public TextView getMessageTextView() {
        return messageTextView;
    }

    public void setMessageTextView(TextView messageTextView) {
        this.messageTextView = messageTextView;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (title == null)
            titleTextView.setVisibility(View.GONE);
        else {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
        }
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }

    public void setTitleTextView(TextView titleTextView) {
        this.titleTextView = titleTextView;
    }

    public ButtonFlat getButtonAccept() {
        return buttonAccept;
    }

    public void setButtonAccept(ButtonFlat buttonAccept) {
        this.buttonAccept = buttonAccept;
    }

    public ButtonFlat getButtonCancel() {
        return buttonCancel;
    }

    public void setButtonCancel(ButtonFlat buttonCancel) {
        this.buttonCancel = buttonCancel;
    }

    public void setOnAcceptButtonClickListener(
            View.OnClickListener onAcceptButtonClickListener) {
        this.onAcceptButtonClickListener = onAcceptButtonClickListener;
        if (buttonAccept != null)
            buttonAccept.setOnClickListener(onAcceptButtonClickListener);
    }

    public void setOnCancelButtonClickListener(
            View.OnClickListener onCancelButtonClickListener) {
        this.onCancelButtonClickListener = onCancelButtonClickListener;
        if (buttonCancel != null)
            buttonCancel.setOnClickListener(onCancelButtonClickListener);
    }

    @Override
    public void dismiss() {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_main_hide_amination);
        anim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        TimeDialog.super.dismiss();
                    }
                });

            }
        });
        Animation backAnim = AnimationUtils.loadAnimation(context, R.anim.dialog_root_hide_amin);

        view.startAnimation(anim);
        backView.startAnimation(backAnim);
    }


}
