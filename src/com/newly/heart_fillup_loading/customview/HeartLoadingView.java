package com.newly.heart_fillup_loading.customview;

import com.newly.heart_fillup_loading.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * 		
 * <ul>
 *   <li>view.setMaxProgress(100);</li>
 *   <li>view.moveToProgress(80);</li>
 *   <li>view.moveToFull();</li>
 *   <li>view.setOnLoadingSucessListener(new onLoadingSucessListener() {};</li>
 * </ul>
 *   
 * 
 *     <com.newly.heart_fillup_loading.customview.HeartLoadingView
        android:id="@+id/heart_loading"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:background="@drawable/heart_loading_bg" />
 *
 */
public class HeartLoadingView extends SurfaceView implements SurfaceHolder.Callback  {
	private int destProgress;
	private onLoadingSucessListener onLoadingSucessListener;
	private float mHeartMostBottomY, mHeartMostTopY, mHeartMostLeftX, mHeartMostRightX;
	private static final int UPDATE_DELAY_TIME = 50;
	private boolean isOverflow;
	private Path mHeartClipPath;
	private int mHeight;
	private int mMaxProgress = 100;
	private int mOffsetY;
	private int mOffsets;
	private Path mWavePath;
	private int mPercent = -1;
	private int mPitchs;
	private float mCurrentProgress;
	private int mRingWidth = -1;
	private int mScrollY; // 剩余的未填充空白的高度,用来控制波浪的高度。
	private Paint mTextPaint;
	private Paint mWaveLayerPaint;
	private Paint mHeartPaint;
	private Paint mHeartFillPaint;

	private int mSpeed;
	private String mText;
	private float mTextBaseLine;
	private float mTextHeight;
	private float mTextWidth;
	private Runnable mUpdateRunnable;
	private int mWaveColor;
	private int mWidth;

	private SurfaceHolder sfh = null;
	private Canvas canvas = null;

	private boolean loop = false;

	private Handler uiHandler;
	private final int LOAD_SUCESS = 0x234567;

	public HeartLoadingView(Context context) {
		this(context, null);
	}

	public HeartLoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init(){
		sfh = this.getHolder();
		sfh.addCallback(this);
		this.setZOrderOnTop(true);
		sfh.setFormat(PixelFormat.TRANSPARENT);

		uiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what == LOAD_SUCESS) {
					if(onLoadingSucessListener != null) {
						onLoadingSucessListener.onLoadingSucess(HeartLoadingView.this);
					}
				}
			}
		};

		DisplayMetrics localDisplayMetrics = getResources().getDisplayMetrics();
		if (mRingWidth == -1) {
			mRingWidth = (Math.min(localDisplayMetrics.widthPixels*7/24,   // 介于屏幕宽度的 1/4 到 1/3 之间
					localDisplayMetrics.heightPixels) / 40);
		}

		mHeight = 2 * mRingWidth + 4 * 10*mRingWidth;
		mWidth = 2 * mRingWidth + 4 * 10*mRingWidth;

		mWaveColor = getResources().getColor(R.color.wave_color);
		mHeartClipPath = new Path();

		mHeartMostBottomY = mHeight*3/5;
		float r = (float) Math.sqrt((mWidth/5)*(mWidth/5)/2);
		mHeartMostTopY = (float) (mHeight/5 - (r - Math.sqrt(r*r/2)));
		mHeartMostLeftX = (float) (mWidth*3/10 - (r - Math.sqrt(r*r/2)));
		mHeartMostRightX = (float) (mWidth*7/10 + (r - Math.sqrt(r*r/2)));
		RectF leftRect = new RectF(mHeartMostLeftX, mHeartMostTopY, mHeartMostLeftX + r*2, mHeartMostTopY + r*2);
		RectF rightRect = new RectF(mHeartMostRightX - r*2, mHeartMostTopY, mHeartMostRightX, mHeartMostTopY + r*2);
		mHeartClipPath.moveTo(mWidth/2, mHeight/5);
		mHeartClipPath.arcTo(leftRect, -45, -180);
		mHeartClipPath.lineTo(mWidth/2, mHeartMostBottomY);
		mHeartClipPath.lineTo(mWidth*7/10, mHeight*2/5);
		mHeartClipPath.arcTo(rightRect, 45, -180);

		mScrollY = (int) mHeartMostBottomY;  //  此处是心型的下端点的y坐标
		mOffsets = 0;
		mWavePath = new Path();
		mOffsetY = (mHeight / 8);
		mSpeed = ((int) (2.0F * ((mHeartMostRightX-mHeartMostLeftX) / 60 )));
		mPitchs = (int) (2 * (mHeartMostRightX-mHeartMostLeftX));

		initPaint();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}


	private void initPaint() {
		// mWaveLayerPaint
		mWaveLayerPaint = new Paint();
		mWaveLayerPaint.setAntiAlias(true);
		mWaveLayerPaint.setStyle(Paint.Style.FILL);
		// mHeartPaint
		mHeartPaint = new Paint();
		mHeartPaint.setAntiAlias(true);
		mHeartPaint.setStyle(Style.STROKE);
		mHeartPaint.setStrokeWidth(5);
		mHeartPaint.setColor(getResources().getColor(R.color.white));
		// mHeartFillPaint
		mHeartFillPaint = new Paint();
		mHeartFillPaint.setAntiAlias(true);
		mHeartFillPaint.setStyle(Style.FILL);
		mHeartFillPaint.setColor(Color.WHITE);
		// mTextPaint
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(mWidth/7);
		mTextPaint.setColor(Color.WHITE);
		updateTextPaint(mPercent + "%");
	}

	/**
	 * 更新textPaint 等待下次ondraw调用
	 *
	 * @param textContent
	 * @param textSize
	 * @param paintColor
	 */
	private void updateTextPaint(String textContent) {
		if (textContent == null)
			return;
		mTextWidth = mTextPaint.measureText(textContent);
		Paint.FontMetrics localFontMetrics = mTextPaint.getFontMetrics();
		mTextHeight = (localFontMetrics.descent - localFontMetrics.ascent);
		mTextBaseLine = (mHeight * 4 / 5 + mTextHeight / 2.0F - localFontMetrics.descent);
	}


	private void start() {
		loop = true;
		if(mUpdateRunnable == null) {
			mUpdateRunnable = new UpdateRunnable();
			new Thread(mUpdateRunnable).start();
		}
	}

	private void stop() {
		loop = false;
	}

	/**
	 * 更新文字显示的百分比
	 */
	private void updatePercent() {
		mPercent = ((int) (100.0F * mCurrentProgress / mMaxProgress));
		if (mText != null) {
			mText = null;
			updateTextPaint(mPercent + "%");
		}
	}

	private void draw() {
		if(isOverflow) {
			stop();
			uiHandler.sendEmptyMessage(LOAD_SUCESS);
			return ;
		}

		try {
			canvas = sfh.lockCanvas();
			if(canvas == null) {
				return;
			}
			/** 清屏 **/
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			/** 绘制 **/
			canvas.drawPath(mHeartClipPath, mHeartFillPaint);
			drawWave(canvas);
			drawHeart(canvas);
			/** 文字 **/
			if (mText != null) {
				canvas.drawText(mText, (mWidth - mTextWidth) / 2.0F, mTextBaseLine,
						mTextPaint);
			} else {
				canvas.drawText(mPercent + "%", (mWidth - mTextWidth) / 2.0F,
						mTextBaseLine, mTextPaint);
			}

			sfh.unlockCanvasAndPost(canvas);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 绘制波浪动画
	 *
	 * @param canvas
	 */
	private void drawWave(Canvas canvas) {
		canvas.save();
		canvas.clipPath(mHeartClipPath, Region.Op.REPLACE); // 背景截取
		mWaveLayerPaint.setColor(mWaveColor);
		canvas.drawPath(mWavePath, mWaveLayerPaint);
		canvas.restore();
	}

	/**
	 * 绘制心形边框
	 * @param canvas
	 */
	private void drawHeart(Canvas canvas) {
		canvas.drawPath(mHeartClipPath, mHeartPaint);
	}

	private void setProgress(float progress) {
		if (progress < 0) {
			mCurrentProgress = 0;
		} else if (mCurrentProgress == progress) {
			return;
		} else {
			mCurrentProgress = progress;
			if (progress < mMaxProgress) {
				mScrollY = ((int)( mHeartMostBottomY - ((mHeartMostBottomY - mHeartMostTopY)*(progress / mMaxProgress)) + 0));
				isOverflow = false;
			} else {
				mScrollY = ((int)( mHeartMostBottomY - ((mHeartMostBottomY - mHeartMostTopY)*(mMaxProgress / mMaxProgress)) + 0));
				isOverflow = true;
			}
		}
		updatePercent();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mWidth, mHeight);
	}

	public interface onLoadingSucessListener {
		public void onLoadingSucess(HeartLoadingView loadingView);
	}

	final class UpdateRunnable implements Runnable {

		public final void run() {
			while(loop){

				if(mCurrentProgress < destProgress) {
					setProgress(mCurrentProgress+2);
				}

				if (!isOverflow && mPercent >= 0) {

					mOffsets = ((mOffsets - mSpeed) % mPitchs);
					mWavePath.reset();
					mWavePath.moveTo(mHeartMostLeftX, mHeartMostBottomY);
					mWavePath.lineTo(mOffsets, mScrollY);

					for (int j = 0;; j++) {
						int k = mOffsets + j * mPitchs/2;
						mWavePath.cubicTo(k + mPitchs / 4, mScrollY - mOffsetY/2, k + mPitchs / 4, mOffsetY/2 + mScrollY, k + mPitchs/2, mScrollY);
						if (k >= (mHeartMostRightX-mHeartMostLeftX)) {
							mWavePath.lineTo(mHeartMostRightX, mHeartMostBottomY);
							mWavePath.close();
							break;
						}
					}
				}
				try {
					Thread.sleep(UPDATE_DELAY_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				draw();
			}

		}
	}

	public void moveToFull(){
		moveToProgress(mMaxProgress);
	}

	public void moveToProgress(int percent) {
		destProgress = percent;
	}

	public void setOnLoadingSucessListener(onLoadingSucessListener listener) {
		onLoadingSucessListener = listener;
	}
	public void reset(){
		if(mUpdateRunnable != null) {
			mUpdateRunnable=null;
		}
		mCurrentProgress = 0;
		isOverflow=false;
		mPercent=0;
	}


}
