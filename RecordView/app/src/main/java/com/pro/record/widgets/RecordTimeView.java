package com.pro.record.widgets;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.pro.record.R;
import com.pro.record.utils.ProUtils;
import com.pro.record.utils.ScreenUtils;

/**
 * 时间进度 自定义View
 */
public class RecordTimeView extends View{

	/** 日志Tag */
	private final String TAG = "RecordTimeView";
    /** View 宽度 */
    private int vWidth = 0;
    /** View 高度 */
    private int vHeight = 0;
    /** 进度图片 宽度 */
    private int bWidth = 0;
    /** 进度图片 高度 */
    private int bHeight = 0;
    /** 字体高度 */
    private int tHeight = 0;
    /** 进度图片 */
    private Bitmap sBitmap;
    /** dip转换px */
    private int dip = 0;
    /** 密度 */
    private float mDensity;
    // ====================
    /** 时间画笔 */
    private Paint mTextPaint;
    /** 线条画笔 */
    private Paint mLinePaint;
    /** 背景进度画笔 */
    private Paint mStepPaint;

	// ================== 构造函数   ===================
	
	public RecordTimeView(Context mContext) {
		super(mContext);
		init();
	}

	public RecordTimeView(Context mContext, AttributeSet attrs) {
		super(mContext, attrs);
		init();
	}
	
	/**
	 * 初始化操作
	 */
	private void init(){
		// 防止不进行绘画 触发onDraw
		setWillNotDraw(false);
		// 获取进度图片
		sBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_location);
        // 屏幕密度
        mDensity = ScreenUtils.getDensity(getContext());
		// 1 dip 对应的px
		dip = ScreenUtils.dipConvertPx(getContext(), 1f);
		// 图片宽高
		bWidth = sBitmap.getWidth();
		bHeight = sBitmap.getHeight();
		// 初始化画笔
		initPaint();
	}
	
	/**
	 * 初始化画笔
	 */
	private void initPaint(){
		// 初始化画笔
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 字体(时间)
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 顶部、底部 直线
		mStepPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 半透明(画布遮挡层)
		// 画笔颜色
		mTextPaint.setColor(Color.rgb(148, 148, 148)); // 字体颜色
		mLinePaint.setColor(Color.rgb(84, 84, 84)); // 线条颜色 #545454
		mStepPaint.setColor(Color.rgb(148, 148, 148)); // 背景进度颜色(画布遮挡层) #949494
		// 设置透明度
		mStepPaint.setAlpha(50); // 画布遮挡层
		mLinePaint.setStrokeWidth(dip * 2f); // 线条高度
		// 设置画笔样式
		mLinePaint.setStyle(Paint.Style.STROKE); // 设置粗线 - 线条
        // 设置字体大小
        setTextSize(14f, false);
	}

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 防止获取宽度高度失败
        if (vWidth == 0 || vHeight == 0){
            vWidth = getWidth();
            vHeight = getHeight();
        } else {
            // 获取字体间隔高度 + 6dip
            int tSpaceHeight = tHeight + dip * 6;
            // 需要绘制文本
            if (isDrawText){
                // 画顶部字体
                for (int i = axisStep - 1; i < vWidth + axisStep - 1; i++) {
                    if (i % (timeInterval) == 0) {  // 显示时间轴的宽度和文字的距离
                        if (i != 0){ // 防止从指定时间开始，当前时间会在最前面（x = 负数的位置, 如果有需求可以把if注释） - 该if 确保前面有 timeIntervalS 的间隔
                            float time = (i * refTime) + cTime; // 显示的时间
                            // 绘制的文本(正常循环5s,10s,15s....,55s,1m,5s..2m,5s...)
                            String sText = ProUtils.secToTimeSecondRetain((int)(time / 1000));
                            // 完整的时间(1m:50s, ....非秒循环)
                            //sText = ProUtils.secToTimeRetain((int)(time / 1000));
                            // 绘制时间
                            canvas.drawText(sText, (i) - axisStep - 10, tHeight, mTextPaint);
                        }
                    }
                }
            } else if (!isKeepText){ // 不需要绘制文本 => 不保留文本位置
                tSpaceHeight = 0;
            }
            // 设置顶部线条位置(绘制的高度,开始位置)
            int tLineY = tSpaceHeight;
            // 计算底部线位置 - View高度 - 图片高度 - 绘制线条的高度
            int bLineY = vHeight - bHeight - (dip * 2);
            // 画顶部线
            canvas.drawLine(0, tLineY, vWidth, tLineY, mLinePaint);
            // 画底部一条线
            canvas.drawLine(0, bLineY, vWidth, bLineY, mLinePaint);
            // 绘制中间进度
            canvas.drawRect(0, tSpaceHeight, moveStep, bLineY, mStepPaint);
            // 设置进度图片(坐标进度图片)
            canvas.drawBitmap(sBitmap, moveStep - (bWidth / 2), vHeight - bHeight, mLinePaint);
        }
    }

    // ==
    /** 是否绘制时间文本 */
    private boolean isDrawText = true;
    /** 是否保留绘制时间文本的位置(不需要绘制时间文本时,可以控制是否保留位置) */
    private boolean isKeepText = true;
    /** 进度图片移动位置 */
    private float moveStep;
    /** 时间轴步数 */
    private int axisStep;
    /** 字体大小 */
    private float textSize = 13f;

    /**
     * 设置字体大小
     * @param textSize
     */
    public void setTextSize(float textSize){
        setTextSize(textSize, true);
    }

    /**
     * 设置字体大小
     * @param textSize
     * @param isRef 是否刷新View
     */
    private void setTextSize(float textSize,  boolean isRef){
        this.textSize = textSize;
        // --
        // 设置画笔大小
        mTextPaint.setTextSize(textSize * mDensity); // 字体大小
        // 获取字体度量(用于计算字体高度)
        FontMetrics fMetrics = mTextPaint.getFontMetrics();
        // 获取字体高度(取整)
        //tHeight = (int) Math.ceil((double)(fMetrics.bottom - fMetrics.top));
        // 获取字体高度(取整) -- 无边距
        tHeight = (int) Math.ceil((double)(fMetrics.descent - fMetrics.ascent));
        // 判断是否刷新
        if (isRef){
            postInvalidate();
        }
    }

    /**
     * 是否保留绘制文本的位置
     * @param keepText
     */
    public void setKeepText(boolean keepText) {
        isKeepText = keepText;
        // --
        if (!isKeepText){
            isDrawText = false;
        }
    }

    /**
     * 是否绘制文本
     * @param drawText
     */
    public void setDrawText(boolean drawText) {
        isDrawText = drawText;
    }

    /** 时间进度还没到中间位置, moveToMidStep进行叠加, 如果到达屏幕中间位置axisStep进行叠加 */
    private void changeMoveAxisParams() {
        if (moveStep <= vWidth / 2) {
            moveStep = moveStep + 1;  //移动的速度
        } else {
            axisStep = axisStep + 1;
        }
    }

    // ===============  回调事件  =================
    /** 录制时间回调 */
    private RecordTimeCallBack rtCallBack;
    /** 录制时间回调 */
    public interface RecordTimeCallBack {
        /**
         * 每秒触发
         * @param dTime 当前时间
         */
        public void preSecond(float dTime);

        /**
         * 开始时触发 - (恢复、开始等)
         * @param dTime 当前时间
         */
        public void start(float dTime);
    }

    /**
     * 设置录制时间回调
     * @param rtCallBack
     */
    public void setRecordTimeCallBack(RecordTimeCallBack rtCallBack) {
        this.rtCallBack = rtCallBack;
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
                    if(rtCallBack != null){
                        rtCallBack.preSecond(displayTime);
                    }
                    break;
            }
        }
    };

    /** 设备连接定时器 */
    private Timer refTimer;
    /** 设备连接定时器任务栈 */
    private TimerTask refTask;
    /** 当前时间 */
    private float cTime = 0l;
    /** 整秒统计 */
    private int iTime = 0;
    /** 刷新时间（毫秒） */
    private int refTime = 20;
    /** 刷新频率 1000 / 刷新时间 */
    private int refRate = 1000 / refTime;
    /** 时间间隔(秒) */
    private int timeIntervalS = 5000; // (5s,10s,15s) - 可以动态修改
    /** 时间间隔 */
    private int timeInterval = timeIntervalS / refTime;
    /** 对外获取时间 */
    private float displayTime = 0l;

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
                    changeMoveAxisParams();
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

    // ===================  对外公开方法   =====================
    /** 开始 */
    public void start(){
        cTime = 0;
        displayTime = 0;
        iTime = 0;
        moveStep = 0f;
        axisStep = 1;
        setTimer(true);
        // --
        if(rtCallBack != null){
            rtCallBack.start(displayTime);
        }
    }

    /**
     * 开始
     * @param time 开始时间(单位毫秒)
     */
    public void start(float time){
        cTime = time;
        displayTime = time;
        iTime = 0;
        moveStep = 0f;
        axisStep = 1;
        setTimer(true);
        // --
        if(rtCallBack != null){
            rtCallBack.start(displayTime);
        }
    }

    /** 停止 */
    public void stop(){
        cTime = 0;
        displayTime = 0;
        iTime = 0;
        moveStep = 0f;
        axisStep = 1;
        // --
        setTimer(false);
    }

    /** 暂停 */
    public void pause(){
        setTimer(false);
    }

    /** 恢复 */
    public void recover(){
        setTimer(true);
        // --
        if(rtCallBack != null){
            rtCallBack.start(displayTime);
        }
    }

    // ======================================

    /**
     * 获取时间，单位毫秒
     * @return
     */
    public float getTime(){
        return displayTime;
    }

    /**
     * 获取时间，单位秒数
     * @return
     */
    public int getTimes(){
        return (int)(displayTime / 1000);
    }
}
