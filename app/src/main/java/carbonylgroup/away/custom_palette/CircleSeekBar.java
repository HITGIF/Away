package carbonylgroup.away.custom_palette;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import carbonylgroup.away.R;
import carbonylgroup.away.activities.MainActivity;

/**
 *
 * CircleSeekBar
 *
 * @author lee
 *
 */

public class CircleSeekBar extends View {

	private final boolean DEBUG = true;
	private final String TAG = "CircleSeekBar";

	private Context mContext = null;
	private AttributeSet mAttrs = null;

	private Drawable mThumbDrawable = null;
	private int mThumbHeight = 0;
	private int mThumbWidth = 0;
	private int[] mThumbNormal = null;
	private int[] mThumbPressed = null;

	private int mSeekBarMax = 0;
	private Paint mSeekBarBackgroundPaint = null;
	private Paint mSeekbarProgressPaint = null;
	private RectF mArcRectF = null;

	private boolean mIsShowProgressText = true;
	private Paint mProgressTextPaint = null;
	private int mProgressTextSize = 0;

	private int mViewHeight = 0;
	private int mViewWidth = 0;
	private int mSeekBarSize = 0;
	private int mSeekBarRadius = 0;
	private int mSeekBarCenterX = 0;
	private int mSeekBarCenterY = 0;
	private float mThumbLeft = 0;
	private float mThumbTop = 0;

	private float mSeekBarDegree = 0;
	private int mCurrentProgress = 1;
	private int mLastProgress = 0;

	private boolean canDrawUp = true;
	private boolean canDrawDown = false;
	private float eventX = 0;
	private float eventY = 0;
	private boolean sliding = false;

	private boolean enability = true;


	public CircleSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mAttrs = attrs;
		initView();
	}

	public CircleSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mAttrs = attrs;
		initView();
	}

	public CircleSeekBar(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	private void initView(){
	//	if(DEBUG) Log.d(TAG, "initView");
		TypedArray localTypedArray = mContext.obtainStyledAttributes(mAttrs, R.styleable.CircleSeekBar);

		//thumb的属性是使用android:thumb属性进行设置的
		//返回的Drawable为一个StateListDrawable类型，即可以实现选中效果的drawable list
		//mThumbNormal和mThumbPressed则是用于设置不同状态的效果，当点击thumb时设置mThumbPressed，否则设置mThumbNormal
		mThumbDrawable = localTypedArray.getDrawable(R.styleable.CircleSeekBar_android_thumb);
		mThumbWidth = this.mThumbDrawable.getIntrinsicWidth();
		mThumbHeight = this.mThumbDrawable.getIntrinsicHeight();

		mThumbNormal = new int[]{-android.R.attr.state_focused, -android.R.attr.state_pressed,
				-android.R.attr.state_selected, -android.R.attr.state_checked};
		mThumbPressed = new int[]{android.R.attr.state_focused, android.R.attr.state_pressed,
				android.R.attr.state_selected, android.R.attr.state_checked};

		float progressWidth = localTypedArray.getDimension(R.styleable.CircleSeekBar_progress_width, 5);
		int progressBackgroundColor = localTypedArray.getColor(R.styleable.CircleSeekBar_progress_background, Color.parseColor("#373e44"));
		int progressFrontColor = localTypedArray.getColor(R.styleable.CircleSeekBar_progress_front, Color.BLUE);
		mSeekBarMax = 35;

		mSeekbarProgressPaint = new Paint();
		mSeekBarBackgroundPaint = new Paint();

		mSeekbarProgressPaint.setColor(progressFrontColor);
		mSeekBarBackgroundPaint.setColor(progressBackgroundColor);

		mSeekbarProgressPaint.setAntiAlias(true);
		mSeekBarBackgroundPaint.setAntiAlias(true);

		mSeekbarProgressPaint.setStyle(Paint.Style.STROKE);
		mSeekBarBackgroundPaint.setStyle(Paint.Style.STROKE);

		mSeekbarProgressPaint.setStrokeWidth(progressWidth);
		mSeekBarBackgroundPaint.setStrokeWidth(progressWidth);

		mArcRectF = new RectF();

		mIsShowProgressText = localTypedArray.getBoolean(R.styleable.CircleSeekBar_show_progress_text, true);
		int progressTextStroke = dip2px(getContext(), 8);
		int progressTextColor = localTypedArray.getColor(R.styleable.CircleSeekBar_progress_text_color, getResources().getColor(R.color.light_gray));

		Typeface typeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/futura_lt_light.ttf");

		mProgressTextSize = dip2px(getContext(), 40);

		mProgressTextPaint = new Paint();
		mProgressTextPaint.setTypeface(typeFace);
		mProgressTextPaint.setColor(progressTextColor);
		mProgressTextPaint.setAntiAlias(true);
		mProgressTextPaint.setStrokeWidth(progressTextStroke);
		mProgressTextPaint.setTextSize(mProgressTextSize);

		localTypedArray.recycle();
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	//	if(DEBUG) Log.d(TAG, "onMeasure");
		mViewWidth = getWidth();
		mViewHeight = getHeight();

		mSeekBarSize = mViewWidth > mViewHeight ? mViewHeight : mViewWidth;

		mSeekBarCenterX = mViewWidth / 2;
		mSeekBarCenterY = mViewHeight / 2;

		mSeekBarRadius = mSeekBarSize / 2 - mThumbWidth / 2;

		int left = mSeekBarCenterX - mSeekBarRadius;
		int right = mSeekBarCenterX + mSeekBarRadius;
		int top = mSeekBarCenterY - mSeekBarRadius;
		int bottom = mSeekBarCenterY + mSeekBarRadius;
		mArcRectF.set(left, top, right, bottom);

		// 起始位置，三点钟方向
		setThumbPosition(Math.toRadians(mSeekBarDegree));
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawCircle(mSeekBarCenterX, mSeekBarCenterY, mSeekBarRadius,
				mSeekBarBackgroundPaint);
		canvas.drawArc(this.mArcRectF, 0.0F, mSeekBarDegree, false, mSeekbarProgressPaint);
		drawThumbBitmap(canvas);
		drawProgressText(canvas);

		super.onDraw(canvas);
	}

	private void drawThumbBitmap(Canvas canvas) {
		this.mThumbDrawable.setBounds((int) mThumbLeft, (int) mThumbTop,
				(int) (mThumbLeft + mThumbWidth), (int) (mThumbTop + mThumbHeight));
		this.mThumbDrawable.draw(canvas);
	}

	private void drawProgressText(Canvas canvas) {
		if (true == mIsShowProgressText){

			String part1 = "0" + String.valueOf((int) mCurrentProgress / 12);
			String part2 = mCurrentProgress%12 < 2 ? "0" + String.valueOf(mCurrentProgress % 12 * 5) : String.valueOf(mCurrentProgress % 12 * 5);
			float textWidth = mProgressTextPaint.measureText(part1 + " : " + part2);
			canvas.drawText(part1 + " : " + part2, mSeekBarCenterX - textWidth / 2 + 5, mSeekBarCenterY + mProgressTextSize / 2 - 15, mProgressTextPaint);
		}
	}

	public void setEnability(boolean enability1)
	{
		enability = enability1;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(enability) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:

					if (isPointOnThumb(event.getX(), event.getY())) {
						seekTo(event.getX(), event.getY(), true);
						sliding = true;
						eventX = event.getX();
						eventY = event.getY();
					}

					break;

				case MotionEvent.ACTION_MOVE:

					if ((canDrawUp || (!canDrawUp && event.getY() < eventY)) || (canDrawDown || (!canDrawDown && event.getY() > eventY)))
						if (sliding) {
							seekTo(event.getX(), event.getY(), false);
							eventX = event.getX();
							eventY = event.getY();
						}

					break;

				case MotionEvent.ACTION_UP:
					sliding = false;
					//	Log.d(TAG, String.valueOf(sliding));
					//seekTo(event.getX(), event.getY(), true);
					break;
			}
		}
		return true;
	}

	private void seekTo(float eventX, float eventY, boolean isUp) {

		mThumbDrawable.setState(mThumbPressed);
		double radian = Math.atan2(eventY - mSeekBarCenterY, eventX - mSeekBarCenterX);
            /*
             * 由于atan2返回的值为[-pi,pi]
             * 因此需要将弧度值转换一下，使得区间为[0,2*pi]
             */
		if (radian < 0){
				radian = radian + 2* Math.PI;
		}
		//	if(DEBUG) Log.e(TAG, "seekTo radian = " + radian);
		setThumbPosition(radian);
		mLastProgress = mCurrentProgress;
		mSeekBarDegree = (float) Math.round(Math.toDegrees(radian));
		mCurrentProgress = (int) (mSeekBarMax * mSeekBarDegree / 360) + 1;



		if(mCurrentProgress == 36 || (mLastProgress >= 33 && mCurrentProgress <= 24)) {
			setProgress(36);
			canDrawUp = false;
			canDrawDown = true;
		}
		else {
			canDrawUp = true;
			canDrawDown = false;
		}
		if(mCurrentProgress == 0 || (mCurrentProgress >= 13 && mLastProgress <= 4)) {
			setProgress(0);
			canDrawUp = true;
			canDrawDown = false;
		}
		else {
			canDrawUp = false;
			canDrawDown = true;
		}
			invalidate();

	}

	private boolean isPointOnThumb(float eventX, float eventY) {
		boolean result = false;
		double distance = Math.sqrt(Math.pow(eventX - mSeekBarCenterX, 2)
				+ Math.pow(eventY - mSeekBarCenterY, 2));
		if (distance < mSeekBarSize && distance > (mSeekBarSize / 2 - mThumbWidth)){
			result = true;
		}
		return result;
	}

	private void setThumbPosition(double radian) {
	//	if(DEBUG) Log.v(TAG, "setThumbPosition radian = " + radian);
		double x = mSeekBarCenterX + mSeekBarRadius * Math.cos(radian);
		double y = mSeekBarCenterY + mSeekBarRadius * Math.sin(radian);
		mThumbLeft = (float) (x - mThumbWidth / 2);
		mThumbTop = (float) (y - mThumbHeight / 2);
	}

	/**
     * 增加set方法，用于在java代码中调用
     */
	public void setProgress(int progress) {
	//	if(DEBUG) Log.v(TAG, "setProgress progress = " + progress);
		if (progress > mSeekBarMax){
			progress = mSeekBarMax;
		}
		if (progress < 0){
			progress = 0;
		}
		mCurrentProgress = progress+1;
		mSeekBarDegree = (progress * 360 / mSeekBarMax);
	//	if(DEBUG) Log.d(TAG, "setProgress mSeekBarDegree = " + mSeekBarDegree);
		setThumbPosition(Math.toRadians(mSeekBarDegree));

		invalidate();
	}

	public int getProgress(){
		return mCurrentProgress;
	}

	public void setProgressMax(int max){
	//	if(DEBUG) Log.v(TAG, "setProgressMax max = " + max);
		mSeekBarMax = max;
	}

	public int getProgressMax(){
		return mSeekBarMax;
	}

	public void setProgressThumb(int thumbId){
		mThumbDrawable = mContext.getResources().getDrawable(thumbId);
	}

	public void setProgressWidth(int width){
	//	if(DEBUG) Log.v(TAG, "setProgressWidth width = " + width);
		mSeekbarProgressPaint.setStrokeWidth(width);
		mSeekBarBackgroundPaint.setStrokeWidth(width);
	}

	public void setProgressBackgroundColor(int color){
		mSeekBarBackgroundPaint.setColor(color);
	}

	public void setProgressFrontColor(int color){
		mSeekbarProgressPaint.setColor(color);
	}

	public void setProgressTextColor(int color){
		mProgressTextPaint.setColor(color);
	}

	public void setProgressTextSize(int size){
	//	if(DEBUG) Log.v(TAG, "setProgressTextSize size = " + size);
		mProgressTextPaint.setTextSize(size);
	}

	public void setProgressTextStrokeWidth(int width){
	//	if(DEBUG) Log.v(TAG, "setProgressTextStrokeWidth width = " + width);
		mProgressTextPaint.setStrokeWidth(width);
	}

	public void setIsShowProgressText(boolean isShow){
		mIsShowProgressText = isShow;
	}
}

