package com.play.pro.widgets;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.play.pro.R;
import com.play.pro.utils.ScreenUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 视频(缩略图、裁剪)进度滑动条
 */
public class VideoSeekBar extends View {

	/** 日志Tag */
	//private final String TAG = "VideoSeekBar";
	// ============== 其他变量 ===============
	/** 滑动的图片(左右两个) */
	private Bitmap leftBitmap, rightBitmap;
	// --
	/** dip转换px */
	private int dip = 0;
	/** 视频路径 */
	private String videoUri = null;
	/** 当前View 宽度 */
	private int vWidth;
	/** 当前View 高度 */
	private int vHeight;
	/** 关键帧时间 */
	private float videoFrame = 0f;
	/** 视频的总长度(毫秒) */
	private int videoDuration = 0;
	/** 当前播放的时间(毫秒) */
	private int videoPlayProgress = 0;
	/** 屏幕上坐标转换时间 - X轴 横 */
	private float xTime = -1f;
	// --
	/** 是否裁剪模式 */
	private boolean isCutMode = false;
	/** 是否清空内存 - 销毁资源*/
	private boolean isClearMemory = true;
	/** 是否绘制播放进度条 */
	private boolean isDrawProgressLine = false;
	/** 是否绘制播放进度背景 */
	private boolean isDrawProgressBG = false;
	// -- 画笔 --
	/** 绘制缩略图画笔 */
	private Paint thumbPaint = new Paint();
	/** 播放进度画笔 */
	private Paint progressPaint = new Paint();
	/** 播放进度背景(阴影层)画笔 */
	private Paint progressBgPaint = new Paint();
	// ============== 缩略图处理 ===============
	/** 缩略图数量 */
	private int thumbCount = 7;
	/** 缩略图Bitmap */
	private Bitmap[] thumbBitmaps;

	public VideoSeekBar(Context context) {
		super(context);
		init();
	}

	public VideoSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VideoSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// --------------------
		// 绘制缩略图 - 防止进行销毁中,导致触发
		if (!isClearMemory && thumbBitmaps != null) {
			// 遍历缩略图数量
			for (int i = 0;i < thumbCount;i++) {
				if(isClearMemory){ // 如果正在回收中,则跳出方法
					break;
				}
				if (thumbBitmaps[i] != null) {
					try {
						// 绘制缩略图
						canvas.drawBitmap(thumbBitmaps[i], i * thumbBitmaps[i].getWidth(), 0, thumbPaint);
					} catch (Exception e) {
					}
				}
			}
		}
		// --------------------
		// 是否绘制播放进度背景(阴影层) - 裁剪模式下滑动也会有这个阴影,所以需要加上是否裁剪模式处理
		if (!isCutMode && isDrawProgressBG){
			// 计算时间防止等于-1 、 防止获取高度失败
			if (getViewWidthConvertTime() != -1f && vHeight != 0){
				// 转换当前的X轴位置(播放进度 / 每个X轴对应的时间)
				float convX = ((float) videoPlayProgress) / xTime;
				// 如果大于等于View的宽度重新设置
				if(convX + dip >= vWidth){
					// 重置位置,直接到结尾
					convX = vWidth - (int) (dip * 1.5);
				}
				// 绘制一个矩形
				canvas.drawRect(0, 0, convX + (dip / 2), vHeight, progressBgPaint);
			}
		}
		// --------------------
		// 是否绘制播放进度条
		if (isDrawProgressLine){
			// 计算时间防止等于-1 、 防止获取高度失败
			if (getViewWidthConvertTime() != -1f && vHeight != 0){
				// 转换当前的X轴位置(播放进度 / 每个X轴对应的时间)
				float convX = ((float) videoPlayProgress) / xTime;
				// 如果大于等于View的宽度重新设置,防止线条回弹(vWidth - convX < dip 导致下次会大于vWidth,线条会回弹)
				if(convX + dip >= vWidth){
					// 重置位置,直接到结尾，显示一条线
					convX = vWidth - (int) (dip * 1.5);
				}
				// 绘制一个矩形(一条线)
				canvas.drawRect(convX, 0, convX + (dip / 2), vHeight, progressPaint);
			}
		}
		// --------------------
		// 判断是否裁剪模式, 并且高度不等于0,防止计算出现问题
		if (isCutMode && vHeight != 0){
			// 计算右边边距值
			reckonRightSX();
			// 绘制左边滑动的X轴位置
			canvas.drawBitmap(leftBitmap, leftSX, 0, thumbPaint);
			// 绘制右边滑动的X轴位置
			canvas.drawBitmap(rightBitmap, rightSX, 0, thumbPaint);
			// === 绘制左边拖动阴影图层 ===
			if (leftSX != 0f){
				// 绘制一个矩形
				canvas.drawRect(0, 0, leftSX, vHeight, progressBgPaint);
			}
			// === 绘制右边拖动阴影图层 ===
			if (rightSX != rightMarginX){
				// 绘制一个矩形
				canvas.drawRect(rightSX + sliderIgWidth, 0, vWidth, vHeight, progressBgPaint);
			}
		}
	}
	
	// ===================== 
	
	// --
	/** 右边的边距（间距图片宽度） */
	private float rightMarginX = 0f;
	/** 左边滑动的X轴 */
	private float leftSX = 0f;
	/** 右边滑动的X轴 */
	private float rightSX = 0f;
	/** 滑动的图片宽度 */
	private int sliderIgWidth = 0;
	// ===
	/** 上次滑动的值 */
	private float oTouchX = -1f;
	/** 旧的中间值 */
	private float lrMiddleX = -1f;
	/** 滑动的View*/
	private int touchView = -1;
	/** 滑动左边的View */
	private final int TOUCH_LEFT_VIEW = 1;
	/** 滑动右边的View */
	private final int TOUCH_RIGHT_VIEW = 2;
	/** 滑动左右两边中间空白部分 */
	private final int TOUCH_MIDST_VIEW = 3;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		// 属于裁剪模式才进行处理
		if(isCutMode){
			// 滑动中的X轴位置
			float xMove = event.getX();
			// --
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: // 按下时
				// 这样判断是刚好在之间,为了增加触摸体验,增加多一般的边距触摸优化
				//if (xMove >= leftSX && xMove <= (leftSX + sliderIgWidth))
				// --
				if (xMove >= (leftSX - sliderIgWidth / 2) && xMove <= (leftSX + ((float) sliderIgWidth) * 1.5)){
					touchView = TOUCH_LEFT_VIEW;
					// 计算滑动距离
					reckonSlide(xMove);
				} else if (xMove >= (rightSX - sliderIgWidth / 2) && xMove <= (rightSX + ((float) sliderIgWidth) * 1.5)){
					touchView = TOUCH_RIGHT_VIEW;
					// 计算滑动距离
					reckonSlide(xMove);
				} else if (xMove >= (leftSX + sliderIgWidth) && xMove <= (rightSX + sliderIgWidth)){ // 属于滑动两个View中间模块
					touchView = TOUCH_MIDST_VIEW;
					// 计算滑动距离
					reckonSlide(xMove);
				} else {  // 表示都没操作
					lrMiddleX = oTouchX = touchView = -1;
				}
				break;
			case MotionEvent.ACTION_MOVE: // 滑动中
				// 计算滑动距离
				reckonSlide(xMove);
				break;
			case MotionEvent.ACTION_UP: // 抬起时
				lrMiddleX = oTouchX = touchView = -1;
				break;
			}
		}
		return true;
	}
	
	/** 计算右边的值 */
	private void reckonRightSX(){
		if(rightMarginX == 0f){
			rightMarginX = vWidth - sliderIgWidth;
		}
		if(rightSX == 0f){ // 默认值为0则表示为最尾端
			rightSX = rightMarginX;
		}
	}
	
	/**
	 * 计算滑动
	 * @param xMove 滑动的X轴
	 */
	private void reckonSlide(float xMove){
		// 计算右边边距值
		reckonRightSX();
		// 如果都不属于滑动,则不处理
		if(!(touchView == TOUCH_LEFT_VIEW || touchView == TOUCH_RIGHT_VIEW
				|| touchView == TOUCH_MIDST_VIEW)){
			return;
		}
		// 转换关键帧相差的X轴位置(关键帧时间 / 每个X轴对应的时间)
		float convX = videoFrame / xTime;
		// 计算间隔宽度(判断是滑动图片宽度大还是关键帧宽度大)
		float spacing = (convX > sliderIgWidth) ? convX : sliderIgWidth;
		// --
		if(touchView == 1){ // 属于滑动左边图片
			// 虚拟位置 = 滑动位置 + 间距宽度
			float vX = xMove + spacing;
			// 判断是否滑动会推动到右边
			if(vX > rightSX){
				// 如果已经给推到边缘了,则进行控制
				if (rightSX >= rightMarginX){
					// 设置右边到边缘
					rightSX = rightMarginX;
					// 左边 = 右边 - 间距宽度(防止重叠)
					leftSX = rightSX - spacing;
				} else { // 如果不在边缘,则进行推
					leftSX = xMove;
					rightSX = xMove + spacing;
				}
			} else { // 如果小于则表示没有触碰到
				leftSX = xMove;
				// 如果边距小于一半则滑动到底部 + 3分之1的边距
				if (xMove <= sliderIgWidth / 2 + sliderIgWidth / 3){
					leftSX = 0f;
				}
			}
			// 最后再进行判断多一次,防止出现意外（快速滑动）
			adjustLoc(TOUCH_LEFT_VIEW, spacing);
		} else if (touchView == 2){ // 属于滑动右边图片
			// 判断是否滑动到边缘(右侧边缘)
			if(xMove >= rightMarginX){ // 滑动到边缘则直接设置边缘
				rightSX = rightMarginX;
			} else {
				// 判断是否触碰到左边 -> 滑动的距离 - 左边的位置 > 边距，表示没触碰
				if (xMove - leftSX > spacing){
					rightSX = xMove;
				} else { // 如果触碰了
					if (leftSX <= 0){ // 如果左边已经到了边缘
						// 设置左边到边缘
						leftSX = 0f;
						// 右边 = 间距宽度
						rightSX = spacing;
					} else { // 左边没到边缘,则进行推
						rightSX = xMove;
						leftSX = rightSX - spacing;
					}
				}
			}
			// 最后再进行判断多一次,防止出现意外（快速滑动）
			adjustLoc(TOUCH_RIGHT_VIEW, spacing);
		} else if (touchView == 3){ // 属于滑动两个View 中间空白的
			// 左右两个的间隔 = 右边减去左边（左边坐标 + 图片宽度）
			float lrSpace = rightSX - leftSX;
			if (lrMiddleX == -1f){
				// 获取中间值
				lrMiddleX = lrSpace;
			}
			// 判断滑动方向
			if (oTouchX == -1f){
				// 记录上次的滑动值
				oTouchX = xMove;
				return;
			}
			if (lrMiddleX > 0){
				// 判断左边是否已经到达最右边
				if(rightSX > rightMarginX){ // 如果已经给推到边缘了,则进行控制
					adjustLoc(TOUCH_MIDST_VIEW, lrMiddleX); // 调整位置
				} else if (leftSX < 0){ // 如果左边的距离等于0
					adjustLoc(TOUCH_MIDST_VIEW, lrMiddleX); // 调整位置
				} else { // 同步位移
					// 判断滑动方向
					if (xMove > oTouchX){ // 往右边滑动
						if (rightSX < rightMarginX){
							rightSX = rightSX + (xMove - oTouchX);
							// --
							leftSX = rightSX - lrMiddleX;
						}
					} else if (xMove < oTouchX){ // 往左边滑动
						if (leftSX > 0){
							leftSX = leftSX - (oTouchX - xMove);
							// --
							rightSX = leftSX + lrMiddleX;
						}
					}
					// 记录上次的滑动值
					oTouchX = xMove;
					// 调整位置
					adjustLoc(TOUCH_MIDST_VIEW, lrMiddleX);
				}
			}
		}
		// 进行绘制
		invalidate();
	}
	
	/**
	 * 调整位置(防止左右超出边缘边距)
	 * @param touchView 滑动的View
	 * @param spacing 两个View间隔的边距
	 */
	private void adjustLoc(int touchView, float spacing){
		// 判断左边是否到达边缘
		if (leftSX <= 0){
			// 设置左边到边缘
			leftSX = 0f;
			// 右边 = 间距宽度
			float tRightSX = spacing;
			// 判断当前位置是否大于边距
			if (rightSX < tRightSX){
				rightSX = tRightSX;
			}
		}
		// --
		// 判断右边是否到达边缘
		if (rightSX >= rightMarginX){
			// 设置右边到边缘
			rightSX = rightMarginX;
			// 左边 = 右边 - 间距宽度(防止重叠)
			float tLeftSX = rightSX - spacing;
			// 判断当前位置是否大于计算出来的位置
			if (leftSX > tLeftSX){
				leftSX = tLeftSX;
			}
		}
	}
	
	// ===========================================

	/**
	 * 初始化操作
	 */
	private void init(){
		// 防止不进行绘画 触发onDraw
		setWillNotDraw(false);
		// 获取左右两个滑动的图片
		leftBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_slider_left);
		rightBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_slider_right);
		// 先保存滑动图片宽度 
		sliderIgWidth = leftBitmap.getWidth();
		// 1 dip 对应的px
		dip = ScreenUtils.dipConvertPx(getContext(), 1.0f);
		// 初始化画笔
		initPaint();
	}
	
	/**
	 * 初始化画笔
	 */
	private void initPaint(){
		// 初始化画笔
		thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 缩略图
		progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 播放进度 白色竖直线条
		progressBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 播放进度背景,半透明(画布遮挡层)
		// 画笔颜色
		progressPaint.setColor(Color.rgb(255, 255, 255)); // 字体颜色 - 白色
		progressBgPaint.setColor(Color.rgb(0, 0, 0)); // 背景进度颜色(画布遮挡层)
		// 设置透明度
		progressBgPaint.setAlpha(60); // 画布遮挡层
		// 设置画笔大小
		progressPaint.setStrokeWidth(dip * 2); // 线条
		// 设置画笔样式
		progressPaint.setStyle(Paint.Style.STROKE); // 设置粗线 - 线条
	}
	
	// ============== 内部计算方法  ==============
	
	/**
	 * 获取View的宽度，转换对应的坐标值 = 时间
	 * @return
	 */
	private float getViewWidthConvertTime(){
		if(xTime == -1f){
			if(vWidth != 0 && videoDuration != 0){
				// 视频总进度 / 宽度 = 每个坐标占用多少毫秒
				xTime = videoDuration / vWidth;
			}
		}
		return xTime;
	}
	
	/**
	 * 获取滑动图片
	 * @param isLeft
	 * @return
	 */
	private Bitmap getSliderBitmap(boolean isLeft){
		// 防止高度为0
		if(vHeight != 0){
			// 获取高度进行计算
			int bHeight = leftBitmap.getHeight();
			// 判断是否需要缩放,高度不一直则要求缩放
			if (bHeight != vHeight){
				// 获取图片宽度
				int bWidth = leftBitmap.getWidth();
				// 计算宽度比例
				bWidth = (int) (((float) vHeight / (float) bHeight) * bWidth);
				// 保存缩放比例后的宽度
				sliderIgWidth = bWidth;
				// 进行比例缩放图片
				leftBitmap = Bitmap.createScaledBitmap(leftBitmap, bWidth, vHeight, true);
				rightBitmap = Bitmap.createScaledBitmap(rightBitmap, bWidth, vHeight, true);
			}
		}
		return isLeft ? leftBitmap : rightBitmap;
	}
	
	// ============== 内部处理方法  ==============
	
	/** 回收缩略图内存 */
	private void clearThumbs(){
		// 销毁资源中
		isClearMemory = true;
		// --
		if(thumbBitmaps != null){
			for(int i = 0, c = thumbBitmaps.length;i < c;i++){
				Bitmap bitmap = thumbBitmaps[i];
				if(bitmap != null){
					if(bitmap != null && !bitmap.isRecycled()){
						try {
							bitmap.recycle();	
						} catch (Exception e) {
						}
					}
					bitmap = null;
				}
			}
		}
	}
	
	/** 创建缩略图 */
	private void buildThumbs(){
		// 先回收旧的内容
		clearThumbs();
		// 重新绘制进行刷新
		postInvalidate();
		// 判断路径是否为null
		if(!TextUtils.isEmpty(videoUri)){
			// 开启后台线程，生成缩略图
			new Thread(btRunn).start();
		}
	}
	
	/** 生成缩略图（来自本地视频） */
	private void buildThumbsToLocal(){
		// 进行创建缩略图(非回收)
		isClearMemory = false;
		// 设置Media构造器
		MediaMetadataRetriever mediaRetriever = new MediaMetadataRetriever();
		try {
			// 防止两个都为默认值
			while(vWidth == 0 || vHeight == 0){
				vWidth = getWidth();
				vHeight = getHeight();
			}
			// 计算每个图片的宽度(宽度 / 总数)
			int btWidth = vWidth / thumbCount;
			// 图片的高度
			int btHeight = vHeight;
			// 设置视频的路径
			mediaRetriever.setDataSource(videoUri);
			// 取得视频的长度(单位为毫秒)
			String vTime = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			// 保存视频总长度(毫秒)
			setVideoDuration(Integer.valueOf(vTime));
			// 进行计算滑动的边距
			getSliderBitmap(true);
			// 计算右边边距值
			reckonRightSX();
			// 获取View的宽度，转换对应的坐标值 = 时间
			getViewWidthConvertTime();
			// 转换时间，然后平分，设置缩略图时间间隔
			int interValSec = Integer.valueOf(vTime) / thumbCount;
			// 初始化缩略图容器
			thumbBitmaps = new Bitmap[thumbCount];
			// 遍历生成缩略图
			for (int i = 0;i < thumbCount;i++) {
				// 计算时间（秒数）
				long timeUs = i * interValSec * 1000;
				// 获取生成缩略图
				Bitmap bitmap = mediaRetriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
				// 保存缩略图
				thumbBitmaps[i] =  ThumbnailUtils.extractThumbnail(bitmap,  btWidth, btHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
				// 刷新界面
				postInvalidate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 释放构造器资源
				mediaRetriever.release();
			} catch (Exception e2) {
			}
		}
	}
	
	/** buildThumbs Runnable 创建缩略图线程 */
	private Runnable btRunn = new Runnable() {
		@Override
		public void run() {
			// 获取文件路径
			File file = new File(videoUri);
			// 判断是否本地文件
			if(file.exists()){
				buildThumbsToLocal();
			}
		}
	};
	
	/**
	 * 设置视频的总长度(内部处理 - MediaMetadataRetriever 获取) - 毫秒
	 * @param videoDuration
	 */
	private void setVideoDuration(int videoDuration){
		this.videoDuration = videoDuration;
	}
	
	
	// ============== 内部控制代码 ===============
	/** 专门刷新View */
	private Handler vhandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0: // 正常进行绘制触发
				postInvalidate();
				break;
			case 1: // 满一秒进行触发
				displayTime = displayTime + 1000; // 累积时间
//				if(rtCallBack != null){
//					rtCallBack.preSecond(displayTime);
//				}
				break;
			}
		}
	};
	
	/** 设备连接定时器 */
	private Timer refTimer;
	/** 设备连接定时器任务栈 */
	private TimerTask refTask;
	/** 整秒统计 */
	private int iTime = 0;
	/** 刷新时间（毫秒） */
	private int refTime = 500; // 250
	/** 刷新频率 1000 / 刷新时间 */
	private int refRate = 1000 / refTime;
	/** 对外获取时间 */
	private long displayTime = 0l;
	
	/**
	 * 设置定时器，刷新View
	 * @param isOpen 是否打开
	 */
	private void setTimer(boolean isOpen) {
		if (isOpen) {
			try {
				if (refTimer != null) {
					refTimer.cancel();
					refTimer = null;
				}
				if (refTask != null) {
					refTask.cancel();
					refTask = null;
				}
			} catch (Exception e) {
			}
			// 开启定时器
			refTimer = new Timer(); // 每次重新new 防止被取消
			// 重新生成定时器 防止出现TimerTask is scheduled already 所以同一个定时器任务只能被放置一次
			refTask = new TimerTask() {
				@Override
				public void run() {
					// 累加播放时间
					videoPlayProgress += refTime;
					// 如果大于总时间则进行重置
					if(videoDuration != 0 && videoPlayProgress > videoDuration){
						videoPlayProgress = videoDuration;
						// 进行通知最后一次
						vhandler.sendEmptyMessage(0);
						// 并且关闭定时器
						setTimer(false);
						return;
					}
					// --
					vhandler.sendEmptyMessage(0);
					++ iTime;
					if(iTime >= refRate){
						vhandler.sendEmptyMessage(1);
						iTime = 0; // 满1秒
					}
				}
			};
			// xx秒后执行，每隔xx秒再执行一次
			refTimer.schedule(refTask, 0, refTime); // 开启定时器
		} else {
			try {
				if (refTimer != null) {
					refTimer.cancel();
					refTimer = null;
				}
				if (refTask != null) {
					refTask.cancel();
					refTask = null;
				}
			} catch (Exception e) {
			}
			vhandler.sendEmptyMessage(0);
		}
	}

	// ============== 对外公开方法 ===============
	
	/**
	 * 销毁操作方法
	 */
	public void destroy(){
		clearThumbs();
	}
	
	/**
	 * 进行重置
	 */
	public void reset(){
		leftSX = 0f; // 重置到最左边
		rightSX = 0f; // 重置到最右边
	}
	
	/**
	 * 是否允许裁剪(判断是否拖动)
	 * @return
	 */
	public boolean isTrimVideo(){
		if(leftSX != 0f || (rightSX != rightMarginX && rightSX != 0f && rightMarginX != 0f)){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取开始时间(左边X轴转换时间) - 毫秒
	 * @return
	 */
	public float getStartTime(){
		if(getViewWidthConvertTime() != -1){
			return leftSX * xTime;
		}
		return -1f;
	}
	
	/**
	 * 获取结束时间(右边X轴转换时间) - 毫秒
	 * @return
	 */
	public float getEndTime(){
		if(getViewWidthConvertTime() != -1){
			return rightSX * xTime;
		}
		return -1f;
	}
	
	/**
	 * 设置视频进度条
	 * @param isCutMode 是否裁剪模式
	 * @param videoUri 视频路径
	 */
	public void setVideoUri(boolean isCutMode, String videoUri) {
		setVideoUri(isCutMode, videoUri, -1f);
	}

	/**
	 * 设置视频进度条
	 * @param isCutMode 是否裁剪模式
	 * @param videoUri 视频路径
	 * @param videoFrame 关键帧时间(毫秒)
	 */
	public void setVideoUri(boolean isCutMode, String videoUri, float videoFrame) {
		this.setCutMode(isCutMode);
		this.videoUri = videoUri;
		this.videoFrame = videoFrame;
		// --
		// 生成缩略图
		buildThumbs();
	}
	
	/**
	 * 设置当前播放进度
	 * @param curTime 当前的时间(毫秒)
	 */
	public void setProgress(int curTime){
		this.videoPlayProgress = curTime;
	}
	
	/**
	 * 是否绘制播放进度条
	 * @param isDrawProgressLine
	 */
	public void setProgressLine(boolean isDrawProgressLine){
		this.isDrawProgressLine = isDrawProgressLine;
		// 判断是否需要开启定时器
		setTimer(this.isDrawProgressLine || this.isDrawProgressBG);
	}
	
	/**
	 * 是否绘制播放进度背景
	 * @param isDrawProgressBG
	 */
	public void setProgressBG(boolean isDrawProgressBG){
		this.isDrawProgressBG = isDrawProgressBG;
		// 判断是否需要开启定时器
		setTimer(this.isDrawProgressLine || this.isDrawProgressBG);
	}
	
	/**
	 * 设置进度绘制相关功能(统一是否显示)
	 * @param isDrawProgress
	 */
	public void setProgressDraw(boolean isDrawProgress){
		this.isDrawProgressLine = isDrawProgress;
		this.isDrawProgressBG = isDrawProgress;
		// 判断是否需要开启定时器
		setTimer(isDrawProgress);
	}
	
	/**
	 * 设置裁剪模式
	 * @param isCutMode 是否裁剪
	 */
	public void setCutMode(boolean isCutMode){
		this.isCutMode = isCutMode;
		// 如果属于裁剪模式,则不绘制背景阴影
		if (isCutMode){
			this.isDrawProgressBG = false;
		}
	}
	
	/**
	 * 设置裁剪模式
	 * @param isCutMode 是否裁剪
	 * @param isDrawProgressLine
	 */
	public void setCutMode(boolean isCutMode, boolean isDrawProgressLine){
		this.setCutMode(isCutMode);
		// --
		this.isDrawProgressLine = isDrawProgressLine;
		// 判断是否需要开启定时器
		setTimer(isDrawProgressLine);
	}
}
