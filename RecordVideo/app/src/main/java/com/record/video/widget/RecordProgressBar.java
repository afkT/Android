package com.record.video.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.record.video.R;

/**
 * detail: 录制进度
 * Created by Ttt
 */
public class RecordProgressBar extends View {

	/** 画笔 */
	private Paint paint;
	/** 圆环的宽度 */
	private float rWidth;
	/** 圆环进度颜色 */
	private int rProgressColor;
	// == 背景图片 ==
	/** 图片 */
	private Bitmap bBitmap;
	/** 图片宽度 */
	private int bWidth = 0;
	/** 图片高度 */
	private int bHeight = 0;
	// ==
	/** 最大进度 */
	private int max = 100;
	/** 当前进度 */
	private int progress;

	public RecordProgressBar(Context context) {
		this(context, null);
	}

	public RecordProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RecordProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 获取进度图片
		bBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.photograph_transcribe);
		// 图片宽高
		bWidth = bBitmap.getWidth();
		bHeight = bBitmap.getHeight();
		// 初始化画笔
		paint = new Paint();
		// 设置默认值
		rProgressColor = Color.parseColor("#92B927");
		rWidth = getResources().getDimension(R.dimen.x10);
	}


	/** 计算高度 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(MeasureSpec.makeMeasureSpec(bWidth, MeasureSpec.getMode(widthMeasureSpec)), MeasureSpec.makeMeasureSpec(bHeight, MeasureSpec.getMode(heightMeasureSpec)));
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 绘制背景
		canvas.drawBitmap(bBitmap, 0, 0, paint);
		// 获取圆心的x坐标
		int centre = bWidth / 2;
		// 圆环的半径
		int radius = (int) (centre - rWidth / 2);
		//设置进度是实心还是空心
		paint.setStrokeWidth(rWidth); // 设置圆环的宽度
		paint.setStyle(Paint.Style.STROKE); // 设置空心
		paint.setColor(rProgressColor);  // 设置进度的颜色
		paint.setAntiAlias(true);  // 消除锯齿
		RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);  // 用于定义的圆弧的形状和大小的界限
		canvas.drawArc(oval, 270, 360 * progress / max, false, paint);  // 根据进度画圆弧
		// 0 从右边开始
		// 270 从上边开始
	}
	
	// ===========
	
	public synchronized int getMax() {
		return max;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(int max) {
		if(max < 0){
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized int getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(int progress) {
		if(progress < 0){
			throw new IllegalArgumentException("progress not less than 0");
		}
		if(progress > max){
			progress = max;
		}
		if(progress <= max){
			this.progress = progress;
			postInvalidate();
		}
	}
	
	public int getRProgressColor() {
		return rProgressColor;
	}

	public void setRProgressColor(int cricleProgressColor) {
		this.rProgressColor = cricleProgressColor;
	}

	public float getRoundWidth() {
		return rWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.rWidth = roundWidth;
	}
}
