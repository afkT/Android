package ttt.scan.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

import dev.utils.LogPrintUtils;
import dev.utils.app.SizeUtils;
import ttt.scan.R;

/**
 * detail: 扫描形状 View
 * Created by Ttt
 *
 * iOS :
 * https://www.jianshu.com/p/d8dd357c0d6c
 *
 * Android:
 * https://blog.csdn.net/Vicent_9920/article/details/78352090
 * https://github.com/yangxixi88/ZxingLite
 *
 * 圆环:
 * https://github.com/wlj644920158/Scanner
 */
public class ScanShapeView extends View {

    // 日志 TAG
    private final String TAG = ScanShapeView.class.getSimpleName();

    // 形状类型 - 默认正方形
    private Shape shapeType = Shape.Square;

    /**
     * detail: 枚举形状类型
     * Created by Ttt
     */
    public enum Shape {

        /** 正方形 */
        Square,

        /** 六边形 */
        Hexagon, // 大小以宽度为准(正方形)

        /** 环形 */
        Annulus, // 以最小的为基准
    }

    /**
     * 便于获取拐角圆角大小
     */
    public static final class CornerEffect extends CornerPathEffect {

        // 拐角圆角大小
        private float radius;

        public CornerEffect(float radius) {
            super(radius);
            this.radius = radius;
        }

        public float getRadius(){
            return radius;
        }
    }

    // 判断是否需要重新处理动画
    private boolean isReAnim = true;
    // 默认通用DP;
    private float dfCommonDP;
    // 空白画笔(绘制边框使用, 不绘制边框时)
    private Paint emptyPaint = new Paint();
    // 是否设置拐角圆角(圆润)
    private CornerEffect cornerEffect = new CornerEffect(10);

    // = 背景相关 =
    // 是否绘制背景
    private boolean isDrawBack = true;
    // 绘制背景画笔
    private Paint backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // = 绘制扫描View =
    // 判断是否绘制扫描区域边框
    private boolean isDrawBorder = true;
    // 扫描边框View
    private Paint borderPaint = new Paint();
    // 边框边距
    private float borderMargin = 0;
    // 边框宽度
    private float borderWidth;
    // 扫描区域块 - 绘制的宽(x), 高(y) - 默认 700x700
    private PointF pointF = new PointF(700, 700);

    // == 正方形(边框相关) ==
    // 正方形描边(边框), 类型 0 = 单独四个角落, 1 = 单独边框, 2 = 全部
    private int borderToSquare = 0;
    // 正方形描边(边框)宽度
    private float borderWidthToSquare;
    // 每个角的点距离(长度)(正方形四个角落区域)
    private float triAngleLength;
    // 是否特殊处理
    private boolean specialToSquare = false;

    // == 环形相关 ==
    // 0 - 外环, 1 - 中间环, 2 - 外环
    // 环形画笔
    private Paint[] annulusPaints = new Paint[3];
    // 三个环宽度
    private float[] annulusWidths = new float[3];
    // 三个环长度
    private int[] annulusLengths = new int[] { 20, 30, 85 };
    // 三个环是否绘制
    private boolean[] annulusDraws = new boolean[] { true, true, true };
    // 三个环分别角度
    private int[] annulusAngles = new int[] { 0, -15, 0 };
    // 三个环颜色值
    private @ColorInt int[] annulusColors = new int[] { Color.BLUE, Color.RED, Color.WHITE};
    // 三个环之间的边距
    private float[] annulusMargins = new float[3];

    // = 动画相关 =
    // 是否绘制动画
    private boolean isDrawAnim = true;
    // 是否自动开启动画
    private boolean isAutoAnim = true;

    // = 正方形(动画) 相关 =
    // 正方形扫描动画 对象
    private ValueAnimator animToSquare;
    // 正方形扫描动画速度(毫秒)
    private long lineDurationToSquare = 10l;
    // 正方形线条画笔
    private Paint linePaintToSquare = new Paint(Paint.ANTI_ALIAS_FLAG);
    // 扫描线条 Bitmap
    private Bitmap bitmapToSquare;
    // 线条偏离值
    private int lineOffsetToSquare = 0;
    // 线条向上(下)边距
    private float lineMarginTopToSquare = 0f;
    // 线条向左(右)边距
    private float lineMarginLeftToSquare = 0f;
    // 线条颜色
    private @ColorInt int lineColorToSquare = 0;

    // = 六边形(动画) 相关 =
    // 边框外动画 对象
    private ValueAnimator animToHexagon;
    // 六边形线条画笔
    private Paint linePaintToHexagon = new Paint();
    // 六边形线条路径
    private Path linePathToHexagon = new Path();
    // 六边形线条 Canvas(动画中实时绘制计算路径)
    private Canvas canvasToHexagon;
    // 六边形线条 绘制出来的 Bitmap
    private Bitmap bitmapToHexagon;
    // 线条中心点
    private float centerToHexagon = 0;
    // 线条宽度
    private float lineWidthToHexagon = 4f;
    // 绘制线条边距(针对绘制区域)
    private float lineMarginToHexagon = 20f;
    // 动画方向(六边形线条) - true = 左, false = 右
    private boolean lineAnimDirection = true;

    // = 环形(动画) 相关 =
    // 环形动画 对象
    private ValueAnimator animToAnnulus;
    // 动画效果临时变量
    private float animOffsetToAnnulus = 0f;
    // 是否达到偏移值最大值
    private  boolean isOffsetMaxToAnnulus = true;
    // 线条向上(下)边距
    private float lineOffsetToAnnulus = 0f;
    // 扫描线条 Bitmap
    private Bitmap bitmapToAnnulus;
    // 线条颜色
    private @ColorInt int lineColorToAnnulus = 0;
    // 绘制扫描线条偏移速度
    private float lineOffsetSpeedToAnnulus = 4f;

    // === 构造函数 ===

    public ScanShapeView(Context context) {
        this(context, null, 0);
    }

    public ScanShapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanShapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        // 关闭硬件加速
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // 初始化
        init();
    }

    /**
     * 初始化处理
     */
    private void init(){
        dfCommonDP = SizeUtils.dipConvertPx(5);
        borderWidth = SizeUtils.dipConvertPx(2);
        borderWidthToSquare = SizeUtils.dipConvertPx(1);
        triAngleLength = SizeUtils.dipConvertPx2(20);
        // =
        annulusWidths[0] = SizeUtils.dipConvertPx(3);
        annulusWidths[1] = SizeUtils.dipConvertPx(7);
        annulusWidths[2] = SizeUtils.dipConvertPx(7);

        annulusMargins[0] = SizeUtils.dipConvertPx(7);
        annulusMargins[1] = SizeUtils.dipConvertPx(7);
        annulusMargins[2] = SizeUtils.dipConvertPx(7);

        // 设置背景颜色 - (黑色 百分之40透明度) #66000000
        backPaint.setColor(Color.argb(102,0,0,0));

        // 扫描边框View 画笔
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);

        // 空白画笔(绘制边框使用, 不绘制边框时)
        emptyPaint.setStrokeWidth(0);
        emptyPaint.setAntiAlias(true);
        emptyPaint.setColor(Color.TRANSPARENT);
        emptyPaint.setStyle(Paint.Style.STROKE);

        // = 环形 =
        // 外环
        annulusPaints[0] = new Paint();
        annulusPaints[0].setColor(annulusColors[0]);
        annulusPaints[0].setAntiAlias(true);
        annulusPaints[0].setStyle(Paint.Style.STROKE);
        annulusPaints[0].setStrokeWidth(annulusWidths[0]);
        // 中间环
        annulusPaints[1] = new Paint();
        annulusPaints[1].setColor(annulusColors[1]);
        annulusPaints[1].setAntiAlias(true);
        annulusPaints[1].setStyle(Paint.Style.STROKE);
        annulusPaints[1].setStrokeWidth(annulusWidths[1]);
        // 内环
        annulusPaints[2] = new Paint();
        annulusPaints[2].setColor(annulusColors[2]);
        annulusPaints[2].setAntiAlias(true);
        annulusPaints[2].setStyle(Paint.Style.STROKE);
        annulusPaints[2].setStrokeWidth(annulusWidths[2]);

        // == 动画相关画笔 ==

        // 六边形线条画笔
        linePaintToHexagon.setStrokeWidth(lineWidthToHexagon);
        linePaintToHexagon.setAntiAlias(true);
        linePaintToHexagon.setStyle(Paint.Style.STROKE);

        // 统一处理画笔拐角
        handlerCornerPathEffect();

        // 加载正方形扫描线条
        bitmapToSquare = ((BitmapDrawable)(getResources().getDrawable(R.drawable.scanline))).getBitmap();

        // 加载圆环扫描
        bitmapToAnnulus = ((BitmapDrawable)(getResources().getDrawable(R.drawable.scanline))).getBitmap();

        // 重置动画处理
        initAnim();
    }

    /**
     * 处理拐角
     */
    private void handlerCornerPathEffect(){
        // 设置绘制边框拐角
        borderPaint.setPathEffect(cornerEffect);
        // 判断是否加入拐角
        switch (shapeType) {
            case Square: // 正方形
                backPaint.setPathEffect(cornerEffect);
                break;
            default: // 其他不设置拐角
                backPaint.setPathEffect(null);
                break;
        }
        // 设置绘制六边形线条拐角
        linePaintToHexagon.setPathEffect(cornerEffect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // == 判断是否绘制背景 ==
        if (isDrawBack){
            // 绘制计算背景
            makeBack(calcShapeRegion(borderMargin), canvas);
        }

        // == 绘制扫描区域(包括边框) ==
        // 绘制计算边框
        makeShape(calcShapeRegion(), canvas, isDrawBorder ? borderPaint : emptyPaint, true);

        // ==============
        // == 动画相关 ==
        // ==============

        // 判断是否需要重新处理动画
        if (isReAnim){ // 为了节省资源, 只用绘制一次
            isReAnim = false;

            // == 绘制扫描动画 ==
            // 判断是否需要动画
            if (isDrawAnim){
                // 计算动画信息
                makeAnim(canvas);
                // 判断是否自动开启动画
                if (isAutoAnim) {
                    // 开始动画
                    startAnim();
                }
            }
        }

        // == 绘制扫描动画 ==
        // 判断是否需要动画
        if (isDrawAnim) {
            drawAnim(canvas);
        }
    }

    // == 外部方法 ==

    /**
     * 销毁处理
     */
    public void destroy(){
        // 停止动画
        stopAnim();
        // 清空Bitmap
        bitmapToSquare = null;
        bitmapToHexagon = null;
        bitmapToAnnulus = null;
    }

    /**
     * 获取扫描形状
     * @return
     */
    public Shape getShapeType() {
        return shapeType;
    }

    /**
     * 设置扫描形状
     * @param shapeType
     * @return
     */
    public ScanShapeView setShapeType(Shape shapeType) {
        if (shapeType == null){
            shapeType = Shape.Square;
        }
        // 停止动画
        stopAnim();
        // 设置类型
        this.shapeType = shapeType;
        // 刷新拐角处理
        handlerCornerPathEffect();
        // 动画处理
        resetAnim(true);
        return this;
    }

    /**
     * 获取拐角角度大小
     * @return
     */
    public float getCornerRadius(){
        if (this.cornerEffect != null){
            return cornerEffect.getRadius();
        }
        return 0f;
    }

    /**
     * 设置是否拐角圆角(主要是控制绘制边框的线) - 部分特殊使用
     * @param cornerEffect
     * @return
     */
    public ScanShapeView setCornerEffect(CornerEffect cornerEffect) {
        this.cornerEffect = cornerEffect;
        // 判断是否小于 0
        if (getCornerRadius() <= 0){
            this.cornerEffect = null;
        }
        // 刷新拐角处理
        handlerCornerPathEffect();
        return this;
    }

    /**
     * 设置扫描区域大小
     * @param wide
     * @return
     */
    public ScanShapeView setRegion(float wide){
        if (wide > 0){
            // 设置宽高
            pointF.x = pointF.y = wide;
        }
        return this;
    }

    /**
     * 设置扫描区域大小
     * @param width
     * @param height
     * @return
     */
    public ScanShapeView setRegion(float width, float height){
        if (width > 0 && height > 0){
            // 设置宽
            pointF.x = width;
            // 设置高
            pointF.y = height;
        }
        return this;
    }

    /**
     * 设置扫描区域大小
     * @param rect or CameraManager.getFramingRect()
     * @return
     */
    public ScanShapeView setRegion(Rect rect){
        if (rect != null){
            // 设置宽
            pointF.x = rect.right - rect.left;
            // 设置高
            pointF.y = rect.bottom - rect.top;
        }
        return this;
    }

    /**
     * 获取扫描绘制区域距离左/右边边距
     * @return
     */
    public float getRegionLeft(){
        return getRegionMarginLeft();
    }

    /**
     * 获取扫描绘制区域距离上/下边边距
     * @return
     */
    public float getRegionTop(){
        return getRegionMarginTop();
    }

    /**
     * 获取扫描区域宽度
     * @return
     */
    public float getRegionWidth(){
        return pointF.x;
    }

    /**
     * 获取扫描区域高度
     * @return
     */
    public float getRegionHeight(){
        return pointF.y;
    }

    /**
     * 获取扫描区域信息
     * @return
     */
    public RectF getRegion(){
        return calcShapeRegion();
    }

    /**
     * 获取扫描区域信息
     * @param left 向左偏差距离(实际屏幕中位置)
     * @param top 向上偏差距离(实际屏幕中位置)
     * @return
     */
    public RectF getRegion(float left, float top){
        // 获取扫描区域
        RectF rectF = calcShapeRegion();
        rectF.left += left;
        rectF.right += left;
        rectF.top += top;
        rectF.bottom += top;
        return rectF;
    }

    /**
     * 获取在父布局中实际的位置 (如该View 没有铺满, 但是为了扫描优化速度, 专门获取扫描区域实际位置)
     * @return
     */
    public RectF getRegionParent(){
        return getRegion(getLeft(), getTop());
    }

    /**
     * 获取边框边距
     * @return
     */
    public float getBorderMargin() {
        return borderMargin;
    }

    /**
     * 设置边框边距
     * @param borderMargin
     * @return
     */
    public ScanShapeView setBorderMargin(float borderMargin) {
        this.borderMargin = borderMargin;
        return this;
    }

    /**
     * 获取边框颜色
     * @return
     */
    public @ColorInt int getBorderColor(){
        return borderPaint.getColor();
    }

    /**
     * 设置边框颜色
     * @param color
     * @return
     */
    public ScanShapeView setBorderColor(@ColorInt int color){
        borderPaint.setColor(color);
        return this;
    }

    /**
     * 获取边框宽度
     * @return
     */
    public float getBorderWidth(){
        return borderPaint.getStrokeWidth();
    }

    /**
     * 设置边框宽度
     * @param width
     * @return
     */
    public ScanShapeView setBorderWidth(float width){
        if (width <= 0){
            width = borderWidth;
        }
        borderPaint.setStrokeWidth(width);
        return this;
    }

    /**
     * 获取是否绘制边框
     * @return
     */
    public boolean isDrawBorder() {
        return isDrawBorder;
    }

    /**
     * 设置是否绘制边框
     * @param drawBorder
     * @return
     */
    public ScanShapeView setDrawBorder(boolean drawBorder) {
        isDrawBorder = drawBorder;
        return this;
    }

    /**
     * 是否绘制背景
     * @return
     */
    public boolean isDrawBack() {
        return isDrawBack;
    }

    /**
     * 设置是否绘制背景
     * @param drawBack
     * @return
     */
    public ScanShapeView setDrawBack(boolean drawBack) {
        isDrawBack = drawBack;
        return this;
    }

    /**
     * 获取绘制的背景颜色
     * @return
     */
    public @ColorInt int getBackColor(){
        return backPaint.getColor();
    }

    /**
     * 设置绘制的背景颜色
     * @param color
     * @return
     */
    public ScanShapeView setBackColor(@ColorInt int color){
        backPaint.setColor(color);
        return this;
    }

    /**
     * 是否绘制动画效果
     * @return
     */
    public boolean isDrawAnim() {
        return isDrawAnim;
    }

    /**
     * 设置是否绘制动画效果
     * @param drawAnim
     * @return
     */
    public ScanShapeView setDrawAnim(boolean drawAnim) {
        isDrawAnim = drawAnim;
        // 动画处理
        resetAnim(true);
        return this;
    }

    /**
     * 是否自动播放动画
     * @return
     */
    public boolean isAutoAnim() {
        return isAutoAnim;
    }

    /**
     * 设置是否自动播放动画
     * @param autoAnim
     * @return
     */
    public ScanShapeView setAutoAnim(boolean autoAnim) {
        isAutoAnim = autoAnim;
        // 动画处理
        resetAnim(true);
        return this;
    }

    // = 正方形 =

    /**
     * 获取 正方形描边(边框), 类型 0 = 单独四个角落, 1 = 单独边框, 2 = 全部
     * @return
     */
    public int getBorderToSquare() {
        return borderToSquare;
    }

    /**
     * 设置 正方形描边(边框), 类型 0 = 单独四个角落, 1 = 单独边框, 2 = 全部
     * @param borderToSquare
     * @return
     */
    public ScanShapeView setBorderToSquare(int borderToSquare) {
        // 防止出现负数
        borderToSquare = Math.max(0, borderToSquare);
        // 防止出现大于异常值
        if (borderToSquare > 2){
            borderToSquare = 0;
        }
        this.borderToSquare = borderToSquare;
        return this;
    }

    /**
     * 获取四个角落与边框共存时, 对应边框宽度
     * @return
     */
    public float getBorderWidthToSquare() {
        return borderWidthToSquare;
    }

    /**
     * 设置四个角落与边框共存时, 对应边框宽度
     * @param borderWidthToSquare
     * @return
     */
    public ScanShapeView setBorderWidthToSquare(float borderWidthToSquare) {
        this.borderWidthToSquare = borderWidthToSquare;
        return this;
    }

    /**
     * 获取每个角的点距离(长度)
     * @return
     */
    public float getTriAngleLength() {
        return triAngleLength;
    }

    /**
     * 设置每个角的点距离(长度)
     * @param triAngleLength
     * @return
     */
    public ScanShapeView setTriAngleLength(float triAngleLength) {
        this.triAngleLength = triAngleLength;
        return this;
    }

    /**
     * 获取特殊处理(正方形边框)
     * @return
     */
    public boolean isSpecialToSquare() {
        return specialToSquare;
    }

    /**
     * 设置特殊处理(正方形边框)
     * @param specialToSquare
     * @return
     */
    public ScanShapeView setSpecialToSquare(boolean specialToSquare) {
        this.specialToSquare = specialToSquare;
        return this;
    }

    /**
     * 获取正方形扫描动画速度(毫秒)
     * @return
     */
    public long getLineDurationToSquare() {
        return lineDurationToSquare;
    }

    /**
     * 设置正方形扫描动画速度(毫秒)
     * @param lineDurationToSquare
     * @return
     */
    public ScanShapeView setLineDurationToSquare(long lineDurationToSquare) {
        if (lineDurationToSquare <= 0){
            lineDurationToSquare = 10l;
        }
        this.lineDurationToSquare = lineDurationToSquare;
        return this;
    }

    /**
     * 获取正方形扫描线条 Bitmap
     * @return
     */
    public Bitmap getBitmapToSquare() {
        return bitmapToSquare;
    }

    /**
     * 设置正方形扫描线条 Bitmap
     * @param bitmapToSquare
     * @return
     */
    public ScanShapeView setBitmapToSquare(Bitmap bitmapToSquare) {
        this.bitmapToSquare = bitmapToSquare;
        // 刷新颜色
        refLineColorToSquare();
        return this;
    }

    /**
     * 获取正方形扫描线条向上(下)边距
     * @return
     */
    public float getLineMarginTopToSquare() {
        return lineMarginTopToSquare;
    }

    /**
     * 设置正方形扫描线条向上(下)边距
     * @param lineMarginTopToSquare
     * @return
     */
    public ScanShapeView setLineMarginTopToSquare(float lineMarginTopToSquare) {
        if (lineMarginTopToSquare < 0f){
            lineMarginTopToSquare = 0f;
        }
        this.lineMarginTopToSquare = lineMarginTopToSquare;
        return this;
    }

    /**
     * 获取正方形扫描线条向左(右)边距
     * @return
     */
    public float getLineMarginLeftToSquare() {
        return lineMarginLeftToSquare;
    }

    /**
     * 设置正方形扫描线条向左(右)边距
     * @param lineMarginLeftToSquare
     * @return
     */
    public ScanShapeView setLineMarginLeftToSquare(float lineMarginLeftToSquare) {
        if (lineMarginLeftToSquare < 0f){
            lineMarginLeftToSquare = 0f;
        }
        this.lineMarginLeftToSquare = lineMarginLeftToSquare;
        return this;
    }

    /**
     * 获取正方形线条动画颜色(着色)
     * @return
     */
    public @ColorInt int getLineColorToSquare() {
        return lineColorToSquare;
    }

    /**
     * 设置正方形线条动画(着色)
     * @param lineColorToSquare
     * @return
     */
    public ScanShapeView setLineColorToSquare(int lineColorToSquare) {
        this.lineColorToSquare = lineColorToSquare;
        // 刷新颜色
        refLineColorToSquare();
        return this;
    }

    // = 六边形 =

    /**
     * 获取六边形线条动画 - 线条宽度
     * @return
     */
    public float getLineWidthToHexagon() {
        return linePaintToHexagon.getStrokeWidth();
    }

    /**
     * 设置六边形线条动画 - 线条宽度
     * @param lineWidthToHexagon
     * @return
     */
    public ScanShapeView setLineWidthToHexagon(float lineWidthToHexagon) {
        if (lineWidthToHexagon <= 0){
            lineWidthToHexagon = this.borderWidth;
        }
        linePaintToHexagon.setStrokeWidth(lineWidthToHexagon);
        return this;
    }

    /**
     * 获取六边形线条动画 - 线条边距
     * @return
     */
    public float getLineMarginToHexagon() {
        return lineMarginToHexagon;
    }

    /**
     * 设置六边形线条动画 - 线条边距
     * @param lineMarginToHexagon
     * @return
     */
    public ScanShapeView setLineMarginToHexagon(float lineMarginToHexagon) {
        this.lineMarginToHexagon = lineMarginToHexagon;
        return this;
    }

    /**
     * 获取六边形线条动画方向 true = 从左到右, false = 从右到左
     * @return
     */
    public boolean isLineAnimDirection() {
        return lineAnimDirection;
    }

    /**
     * 设置六边形线条动画方向 true = 从左到右, false = 从右到左
     * @param lineAnimDirection
     * @return
     */
    public ScanShapeView setLineAnimDirection(boolean lineAnimDirection) {
        this.lineAnimDirection = lineAnimDirection;
        return this;
    }

    /**
     * 获取六边形线条动画颜色
     * @return
     */
    public @ColorInt int getLineColorToHexagon() {
        return lineColorToHexagon;
    }

    /**
     * 设置六边形线条动画颜色
     * @param lineColorToHexagon
     * @return
     */
    public ScanShapeView setLineColorToHexagon(int lineColorToHexagon) {
        this.lineColorToHexagon = lineColorToHexagon;
        // 刷新颜色
        refLineColorToHexagon();
        return this;
    }

    // == 环形 ==

    /**
     * 获取环形扫描线条 Bitmap
     * @return
     */
    public Bitmap getBitmapToAnnulus() {
        return bitmapToAnnulus;
    }

    /**
     * 设置环形扫描线条 Bitmap
     * @param bitmapToAnnulus
     * @return
     */
    public ScanShapeView setBitmapToAnnulus(Bitmap bitmapToAnnulus) {
        this.bitmapToAnnulus = bitmapToAnnulus;
        // 刷新颜色
        refLineColorToAnnulus();
        return this;
    }

    /**
     * 获取环形线条动画颜色(着色)
     * @return
     */
    public @ColorInt int getLineColorToAnnulus() {
        return lineColorToAnnulus;
    }

    /**
     * 设置环形线条动画(着色)
     * @param lineColorToAnnulus
     * @return
     */
    public ScanShapeView setLineColorToAnnulus(int lineColorToAnnulus) {
        this.lineColorToAnnulus = lineColorToAnnulus;
        // 刷新颜色
        refLineColorToAnnulus();
        return this;
    }

    /**
     * 获取环形扫描线条速度
     * @return
     */
    public float getLineOffsetSpeedToAnnulus() {
        return lineOffsetSpeedToAnnulus;
    }

    /**
     * 设置环形扫描线条速度
     * @param lineOffsetSpeedToAnnulus 尽量接近3-6
     * @return
     */
    public ScanShapeView setLineOffsetSpeedToAnnulus(float lineOffsetSpeedToAnnulus) {
        if (lineOffsetSpeedToAnnulus < 0){
            lineOffsetSpeedToAnnulus = 4f;
        }
        this.lineOffsetSpeedToAnnulus = lineOffsetSpeedToAnnulus;
        return this;
    }

    /**
     * 获取环形对应的环是否绘制 0 - 外环, 1 - 中间环, 2 - 外环
     * @return
     */
    public boolean[] getAnnulusDraws() {
        return annulusDraws;
    }

    /**
     * 设置环形对应的环是否绘制 0 - 外环, 1 - 中间环, 2 - 外环
     * @param annulusDraws
     * @return
     */
    public ScanShapeView setAnnulusDraws(boolean... annulusDraws) {
        if (annulusDraws == null){
            annulusDraws = new boolean[] { true, true, true};
        }
        // 设置临时数据
        boolean[] temp = Arrays.copyOf(annulusDraws, 3);
        // 如果小于3位, 则特殊处理
        if (annulusDraws.length < 3){
            // 没有传递的, 则使用之前的配置
            for (int i = annulusDraws.length; i < 3; i++){
                temp[i] = this.annulusDraws[i];
            }
        }
        this.annulusDraws = temp;
        return this;
    }

    /**
     * 获取环形对应的环绘制颜色 0 - 外环, 1 - 中间环, 2 - 外环
     * @return
     */
    public int[] getAnnulusColors() {
        return annulusColors;
    }

    /**
     * 设置环形对应的环绘制颜色 0 - 外环, 1 - 中间环, 2 - 外环
     * @param annulusColors
     * @return
     */
    public ScanShapeView setAnnulusColors(@ColorInt int... annulusColors) {
        if (annulusColors == null){
            annulusColors = new int[] { Color.BLUE, Color.RED, Color.WHITE};
        }
        // 设置临时数据
        int[] temp = Arrays.copyOf(annulusColors, 3);
        // 如果小于3位, 则特殊处理
        if (annulusColors.length < 3){
            // 没有传递的, 则使用之前的配置
            for (int i = annulusColors.length; i < 3; i++){
                temp[i] = this.annulusColors[i];
            }
        }
        this.annulusColors = temp;
        // 刷新环形画笔信息
        refPaintToAnnulus();
        return this;
    }

    /**
     * 获取环形对应的环绘制长度 0 - 外环, 1 - 中间环, 2 - 外环
     * @return
     */
    public int[] getAnnulusLengths() {
        return annulusLengths;
    }

    /**
     * 设置环形对应的环绘制长度 0 - 外环, 1 - 中间环, 2 - 外环
     * @param annulusLengths
     * @return
     */
    public ScanShapeView setAnnulusLengths(int... annulusLengths) {
        if (annulusLengths == null){
            annulusLengths = new int[] { 20, 30, 85 };
        }
        // 设置临时数据
        int[] temp = Arrays.copyOf(annulusLengths, 3);
        // 如果小于3位, 则特殊处理
        if (annulusLengths.length < 3){
            // 没有传递的, 则使用之前的配置
            for (int i = annulusLengths.length; i < 3; i++){
                temp[i] = this.annulusLengths[i];
            }
        }
        this.annulusLengths = temp;
        return this;
    }

    /**
     * 获取环形对应的环绘制宽度 0 - 外环, 1 - 中间环, 2 - 外环
     * @return
     */
    public float[] getAnnulusWidths() {
        return annulusWidths;
    }

    /**
     * 设置环形对应的环绘制宽度 0 - 外环, 1 - 中间环, 2 - 外环
     * @param annulusWidths
     * @return
     */
    public ScanShapeView setAnnulusWidths(float... annulusWidths) {
        if (annulusWidths == null){
            annulusWidths = new float[3];
            annulusWidths[0] = SizeUtils.dipConvertPx(3);
            annulusWidths[1] = SizeUtils.dipConvertPx(7);
            annulusWidths[2] = SizeUtils.dipConvertPx(7);
        }
        // 设置临时数据
        float[] temp = Arrays.copyOf(annulusWidths, 3);
        // 如果小于3位, 则特殊处理
        if (annulusWidths.length < 3){
            // 没有传递的, 则使用之前的配置
            for (int i = annulusWidths.length; i < 3; i++){
                temp[i] = this.annulusWidths[i];
            }
        }
        this.annulusWidths = temp;
        // 刷新环形画笔信息
        refPaintToAnnulus();
        return this;
    }

    /**
     * 获取环形对应的环绘制边距 0 - 外环, 1 - 中间环, 2 - 外环
     * @return
     */
    public float[] getAnnulusMargins() {
        return annulusMargins;
    }

    /**
     * 设置环形对应的环绘制边距 0 - 外环, 1 - 中间环, 2 - 外环
     * @param annulusMargins
     * @return
     */
    public ScanShapeView setAnnulusMargins(float... annulusMargins) {
        if (annulusMargins == null){
            int dp = SizeUtils.dipConvertPx(7);
            annulusMargins = new float[] { dp, dp, dp };
        }
        // 设置临时数据
        float[] temp = Arrays.copyOf(annulusMargins, 3);
        // 如果小于3位, 则特殊处理
        if (annulusMargins.length < 3){
            // 没有传递的, 则使用之前的配置
            for (int i = annulusMargins.length; i < 3; i++){
                temp[i] = this.annulusMargins[i];
            }
        }
        this.annulusMargins = temp;
        return this;
    }

    // annulusLengths

    // == 内部处理方法 ==

//    /**
//     * 复制配置信息处理
//     * @param dfValues 默认数据
//     * @param original 原始数据
//     * @param length
//     * @param values
//     * @param <T>
//     * @return
//     */
//    private <T> void copyValues(T[] dfValues, T[] original, int length, T... values){
//        if (values == null){
//            values = dfValues;
//        }
//        // 设置临时数据
//        T[] temp = Arrays.copyOf(values, length);
//        // 如果小于3位, 则特殊处理
//        if (values.length < length){
//            // 没有传递的, 则使用之前的配置
//            for (int i = values.length; i < length; i++){
//                temp[i] = original[i];
//            }
//        }
//        original = temp;
//    }

    /**
     * 刷新环形画笔信息
     */
    private void refPaintToAnnulus(){
        // 循环重置宽度, 颜色值
        for (int i = 0; i < 3; i++){
            annulusPaints[i].setColor(annulusColors[i]);
            annulusPaints[i].setStrokeWidth(annulusWidths[i]);
        }
    }

    // == 计算相关 ==

    /**
     * Math.sin的参数为弧度，使用起来不方便，重新封装一个根据角度求sin的方法
     * @param num 角度
     * @return
     */
    private float sin(int num){
        return (float) Math.sin(num* Math.PI / 180);
    }

    /**
     * 获取扫描区域左边边距(左右相等) = (View 宽度 - 扫描区域宽度) / 2
     * @return
     */
    private float getRegionMarginLeft(){
        return (getWidth() - pointF.x) / 2;
    }

    /**
     * 获取扫描区域向上边距(上下相等) = (View 宽度 - 扫描区域宽度) / 2
     * @return
     */
    private float getRegionMarginTop(){
        return (getHeight() - pointF.y) / 2;
    }

    /**
     * 计算扫描区域, 并返回区域信息
     * @return
     */
    private RectF calcShapeRegion(){
        return calcShapeRegion(0f);
    }

    /**
     * 计算扫描区域, 并返回区域信息
     * @param margin 边距
     * @return
     */
    private RectF calcShapeRegion(float margin){
        // 获取左边边距
        float left = getRegionMarginLeft();
        // 获取向上边距
        float top = getRegionMarginTop();
        // 生成扫描区域信息
        RectF rectF = new RectF(left - margin, top - margin, pointF.x + left + margin, pointF.y + top + margin);
        // 返回计算后的扫描区域
        return rectF;
    }

    // == 绘制形状 ==

    /**
     * 绘制计算形状(边框外形)
     * @param rectF 绘制区域块
     * @param canvas 画布
     * @param paint 画笔
     * @param isDraw 是否进行绘制
     * @return
     */
    private Path makeShape(RectF rectF, Canvas canvas, Paint paint, boolean isDraw) {
        // 绘制路径
        Path path = new Path();

        // 位置信息
        float r = (rectF.right - rectF.left) / 2; // 半径
        float mX = (rectF.right + rectF.left) / 2; // X 轴中心点位置
        float mY = (rectF.top + rectF.bottom) / 2; // Y 轴中心点位置

        // 判断形状类型
        switch (shapeType){
            case Square: // 正方形
                boolean[] isBorderToSquare = new boolean[] { false, false };
                // 判断正方形描边类型
                switch (borderToSquare){
                    case 0: // 表示只需要四个角落
                        isBorderToSquare[1] = true;
                        break;
                    case 1: // 表示只需要边缘
                        isBorderToSquare[0] = true;
                        break;
                    case 2: // 表示全部绘制
                        isBorderToSquare[0] = true;
                        isBorderToSquare[1] = true;
                        break;
                    default: // 默认只需要四个角落
                        isBorderToSquare[1] = true;
                        break;
                }
                // ===

                if (isBorderToSquare[0]) {
                    Paint borderPaint = new Paint(paint);
                    // 判断是否也显示 角落, 是的话才重置
                    if (isBorderToSquare[1]) {
                        borderPaint.setStrokeWidth(borderWidthToSquare);
                    }
                    // 完整绘制的横线 (正方形)
                    path.moveTo(mX, mY - r); // 设置起始点
                    // 左边
                    path.lineTo(mX - r, mY - r); // 左边第一条上横线
                    path.lineTo(mX - r, mY + r); // 左边第二条竖线
//                    path.lineTo(mX - r, mY + r); // 左边第三条下横线 // 不绘制不会有直角
                    // 右边
                    path.lineTo(mX + r, mY + r); // 右边边第一条下横线
                    path.lineTo(mX + r, mY - r); // 右边边第二条竖线
                    path.close();
                    // 进行绘制
                    canvas.drawPath(path, borderPaint);
                }

                if (isBorderToSquare[1]){
                    Paint borderPaint = new Paint(paint);
                    // 判断是否特殊处理
                    if (specialToSquare) {
                        // 如果已经绘制边框, 则不设置圆角
                        if (isBorderToSquare[0] && borderPaint.getPathEffect() != null) {
                            borderPaint.setPathEffect(null);
                        }
                    }

                    rectF.left += borderWidth / 2;
                    rectF.top += borderWidth / 2;
                    rectF.right -= borderWidth / 2;
                    rectF.bottom -= borderWidth / 2;
                    // 四个角落的三角
                    Path leftTopPath = new Path();
                    leftTopPath.moveTo(rectF.left + triAngleLength, rectF.top);
                    leftTopPath.lineTo(rectF.left, rectF.top);
                    leftTopPath.lineTo(rectF.left, rectF.top + triAngleLength);
                    canvas.drawPath(leftTopPath, borderPaint);

                    Path rightTopPath = new Path();
                    rightTopPath.moveTo(rectF.right - triAngleLength, rectF.top);
                    rightTopPath.lineTo(rectF.right, rectF.top);
                    rightTopPath.lineTo(rectF.right, rectF.top + triAngleLength);
                    canvas.drawPath(rightTopPath, borderPaint);

                    Path leftBottomPath = new Path();
                    leftBottomPath.moveTo(rectF.left, rectF.bottom - triAngleLength);
                    leftBottomPath.lineTo(rectF.left, rectF.bottom);
                    leftBottomPath.lineTo(rectF.left + triAngleLength, rectF.bottom);
                    canvas.drawPath(leftBottomPath, borderPaint);

                    Path rightBottomPath = new Path();
                    rightBottomPath.moveTo(rectF.right - triAngleLength, rectF.bottom);
                    rightBottomPath.lineTo(rectF.right, rectF.bottom);
                    rightBottomPath.lineTo(rectF.right, rectF.bottom - triAngleLength);
                    canvas.drawPath(rightBottomPath, borderPaint);
                }
                break;
            case Hexagon: // 六方形
                // 对应6条线角度计算
                path.moveTo(mX,mY - r); // 1
                path.lineTo(mX + r * sin(60),mY - r / 2); // 3
                path.lineTo(mX + r * sin(60),mY + r / 2); // 5
                path.lineTo(mX,mY + r); // 6
                path.lineTo(mX - r * sin(60),mY + r / 2); // 4
                path.lineTo(mX - r * sin(60),mY - r / 2); // 2
                path.close();
                // 判断是否需要绘制
                if (isDraw) {
                    // 进行绘制
                    canvas.drawPath(path, paint);
                }
                break;
            case Annulus: // 环形
                // 判断是否动画中
                if (isAnimRunning()){
                    // 判断是否绘制最外层
                    if (annulusDraws[0]){
                        // 第一个小弧度
                        canvas.drawArc(rectF, annulusAngles[0], annulusLengths[0], false, annulusPaints[0]);
                        // 第二个小弧度
                        canvas.drawArc(rectF, annulusAngles[0] + 180, annulusLengths[0], false, annulusPaints[0]);
                    }
                    // 判断是否绘制中间层
                    if (annulusDraws[1]){
                        // 计算缩放动画偏移
                        if (animOffsetToAnnulus > 0){
                            animOffsetToAnnulus -= 2;
                        } else {
                            animOffsetToAnnulus = 0f;
                        }
                        // 计算中间层间隔距离
                        float middleSpace = annulusWidths[0] + annulusWidths[1] + annulusMargins[0] + animOffsetToAnnulus;
                        canvas.drawCircle(mX, mY, r - middleSpace, annulusPaints[1]);
                        // ==============
                        // 中间层, 两个弧
                        if (animOffsetToAnnulus == 0f && annulusMargins[0] / 2 > 0f) { // 小于 0 则不绘制
                            // 计算中间层边距
                            float middleMargin = annulusWidths[0] + annulusWidths[1] + annulusMargins[0] / 2;
                            // 计算新的路径
                            RectF outsiderRectF = new RectF(rectF);
                            outsiderRectF.left += middleMargin;
                            outsiderRectF.top += middleMargin;
                            outsiderRectF.right -= middleMargin;
                            outsiderRectF.bottom -= middleMargin;
                            // 第一个小弧度
                            canvas.drawArc(outsiderRectF, annulusAngles[1], annulusLengths[1], false, annulusPaints[1]);
                            // 第二个小弧度
                            canvas.drawArc(outsiderRectF, annulusAngles[1] + 180, annulusLengths[1], false, annulusPaints[1]);
                        }
                    }

                    // 判断是否绘制最内层
                    if (annulusDraws[2]){
                        // 计算最内层间隔距离
                        float insideSpace = annulusWidths[0] + annulusWidths[1] + annulusWidths[2] + annulusMargins[0];
                        // 计算新的路径
                        RectF outsiderRectF = new RectF(rectF);
                        outsiderRectF.left += insideSpace;
                        outsiderRectF.top += insideSpace;
                        outsiderRectF.right -= insideSpace;
                        outsiderRectF.bottom -= insideSpace;
                        // 绘制最内层, 4个弧
                        canvas.drawArc(outsiderRectF, annulusAngles[2], annulusLengths[2], false, annulusPaints[2]);
                        canvas.drawArc(outsiderRectF, annulusAngles[2] + 90, annulusLengths[2], false, annulusPaints[2]);
                        canvas.drawArc(outsiderRectF, annulusAngles[2] + 180, annulusLengths[2], false, annulusPaints[2]);
                        canvas.drawArc(outsiderRectF, annulusAngles[2] + 270, annulusLengths[2], false, annulusPaints[2]);
                    }
                } else { // 停止结束状态
                    // 判断绘制动画效果(只有结束后, 做的一个动画效果)
                    boolean isDrawAnim = false;
                    // 判断是否绘制中间层
                    if (annulusDraws[1]){
                        // 计算中间层间隔距离
                        float middleSpace = annulusWidths[0] + annulusWidths[1] + annulusMargins[0] + animOffsetToAnnulus;
                        // 计算最内层间隔距离
                        float insideSpace = annulusWidths[0] + annulusWidths[1] + annulusWidths[2] + annulusMargins[0];
                        // 计算缩放动画偏移
                        if (middleSpace < insideSpace + annulusWidths[1]) {
                            middleSpace += 2;
                            animOffsetToAnnulus += 2;
                            isDrawAnim = true;
                        }
                        canvas.drawCircle(mX, mY, r - middleSpace, annulusPaints[1]);
                    }

                    // 判断是否绘制最内层
                    if (annulusDraws[2]){
                        // 计算最内层间隔距离
                        float insideSpace = annulusWidths[0] + annulusWidths[1] + annulusWidths[2] + annulusMargins[0];
                        // 计算新的路径
                        RectF outsiderRectF = new RectF(rectF);
                        outsiderRectF.left += insideSpace;
                        outsiderRectF.top += insideSpace;
                        outsiderRectF.right -= insideSpace;
                        outsiderRectF.bottom -= insideSpace;
                        // 绘制最内层, 4个弧
                        canvas.drawArc(outsiderRectF, annulusAngles[2], annulusLengths[2], false, annulusPaints[2]);
                        canvas.drawArc(outsiderRectF, annulusAngles[2] + 90, annulusLengths[2], false, annulusPaints[2]);
                        canvas.drawArc(outsiderRectF, annulusAngles[2] + 180, annulusLengths[2], false, annulusPaints[2]);
                        canvas.drawArc(outsiderRectF, annulusAngles[2] + 270, annulusLengths[2], false, annulusPaints[2]);
                    }

                    // 设置是否绘制
                    if (isDrawAnim){
                        postInvalidate();
                    }
                }
                break;
        }
        return path;
    }

    /**
     * 绘制背景
     * @param rectF
     * @param canvas
     */
    private void makeBack(RectF rectF, Canvas canvas){
        // 都小于0, 则不处理
        if (rectF.left <= 0 && rectF.top <= 0){
            return;
        }

        Path leftPath = new Path(); // 左边路径
        Path rightPath = new Path(); // 右边路径
        // 位置信息
        float r = (rectF.right - rectF.left) / 2; // 半径
        float mX = (rectF.right + rectF.left) / 2; // X 轴中心点位置
        float mY = (rectF.top + rectF.bottom) / 2; // Y 轴中心点位置

        // 获取 View 宽度
        final int width = getWidth();
        // 获取 View 高度
        final int height = getHeight();

        // 判断形状类型
        switch (shapeType){
            case Square: // 正方形
                // 因为使用正方形, 如果使用圆角, 在拐角处, 会有圆圈, 所以拐角处, 统一加大边距处理
                // 解决路径拐角有圆圈，透过底层颜色

                // 获取拐角大小
                float radius = getCornerRadius();
                // =======
                leftPath.moveTo(mX, 0); // 设置起始点
                leftPath.lineTo(0 - radius, 0); // 从中间到顶部边缘
                leftPath.lineTo(0 - radius, height + radius); // 从顶部到最下面
                leftPath.lineTo(mX + radius, height + radius); // 底部到中间点
                leftPath.lineTo(mX + radius, rectF.bottom); // 中间点到区域底部
                leftPath.lineTo(mX + radius, rectF.bottom); // 再次绘制覆盖左侧底部中间上方拐角
                leftPath.lineTo(rectF.left, rectF.bottom); // 区域底部到区域左
                leftPath.lineTo(rectF.left, rectF.top); // 区域底部到区域顶部
                leftPath.lineTo(mX + radius, rectF.top); // 区域顶部到(顶部)中心点
                leftPath.lineTo(mX + radius, rectF.top); // 再次绘制覆盖左侧顶部中间上方拐角
                leftPath.lineTo(mX + radius, -radius); // 回到起始点
                leftPath.close();
                // 进行绘制背景
                canvas.drawPath(leftPath, backPaint);
                // =======
                rightPath.moveTo(mX, 0); // 设置起始点
                rightPath.lineTo(width + radius, 0); // 从中间到顶部边缘
                rightPath.lineTo(width + radius, height + radius); // 从顶部到最下面
                rightPath.lineTo(mX + radius, height + radius); // 底部到中间点
                rightPath.lineTo(mX + radius, rectF.bottom); // 中间点到区域底部
                rightPath.lineTo(mX + radius, rectF.bottom); // 再次绘制覆盖右侧底部中间上方拐角
                rightPath.lineTo(rectF.right, rectF.bottom); // 区域底部到区域右
                rightPath.lineTo(rectF.right, rectF.top); // 区域底部到区域顶部
                rightPath.lineTo(mX + radius, rectF.top); // 区域顶部到(顶部)中心点
                rightPath.lineTo(mX + radius, rectF.top); // 再次绘制覆盖右侧顶部中间上方拐角
                rightPath.lineTo(mX + radius, -radius); // 回到起始点
                rightPath.close();
                // 进行绘制背景
                canvas.drawPath(rightPath, backPaint);
                break;
            case Hexagon: // 六方形
                leftPath.moveTo(0,0); // 左上
                leftPath.lineTo(width / 2,0); // 顶部中心点
                leftPath.lineTo(mX,mY - r); // 1
                leftPath.lineTo(mX - r * sin(60),mY - r / 2); // 2
                leftPath.lineTo(mX - r * sin(60),mY + r / 2); // 4
                leftPath.lineTo(mX,mY + r); // 6
                leftPath.lineTo(width / 2, height); // 底部中心点
                leftPath.lineTo(0, height); // 左下
                leftPath.close();
                // 进行绘制背景
                canvas.drawPath(leftPath, backPaint);
                // =======
                rightPath.moveTo(width,0); // 右上
                rightPath.lineTo(width / 2,0); // 顶部中心点
                rightPath.lineTo(mX,mY - r); // 1
                rightPath.lineTo(mX + r * sin(60),mY - r / 2); // 3
                rightPath.lineTo(mX + r * sin(60),mY + r / 2); // 5
                rightPath.lineTo(mX,mY + r); // 6
                rightPath.lineTo(width / 2, height); // 底部中心点
                rightPath.lineTo(width, height); // 右下
                rightPath.close();
                // 进行绘制背景
                canvas.drawPath(rightPath, backPaint);
                break;
            case Annulus: // 环形
                leftPath.moveTo(mX,0); // 中心点
                leftPath.lineTo(0,0); // 顶部最左边
                leftPath.lineTo(0, height); // 底部最左边
                leftPath.lineTo(mX, height); // 底部中间
                leftPath.lineTo(mX, rectF.bottom); // 底部 bottom 位置
                leftPath.arcTo(rectF, -270, 180); // 从第三象限到第一象限
                //leftPath.lineTo(mX, rectF.top);
                leftPath.lineTo(mX, 0);
                leftPath.close();
                // 进行绘制背景
                canvas.drawPath(leftPath, backPaint);
                // =
                rightPath.moveTo(mX,0); // 中心点
                rightPath.lineTo(width,0); // 顶部最右边
                rightPath.lineTo(width, height); // 底部最左边
                rightPath.lineTo(mX, height); // 底部中间
                rightPath.lineTo(mX, rectF.bottom); // 底部 bottom 位置
                rightPath.arcTo(rectF, -270, -180); // 从第三象限到第一象限
                //rightPath.lineTo(mX, rectF.top);
                rightPath.lineTo(mX, 0);
                rightPath.close();
                // 进行绘制背景
                canvas.drawPath(rightPath, backPaint);
                break;
        }
    }

    /**
     * 计算动画相关信息
     * @param canvas
     */
    private void makeAnim(Canvas canvas){
        // 判断形状类型
        switch (shapeType) {
            case Square: // 正方形
                // 正方形不需要绘制计算(初始化)
                break;
            case Hexagon: // 六边形
                // 获取绘制的区域(绘制扫描区域 + 线条居于绘制边框距离)
                RectF rectF = calcShapeRegion(lineMarginToHexagon);
                // 线条路径计算重置起始位置
                RectF lineRectF = new RectF(0, 0, rectF.right - rectF.left, rectF.right - rectF.left);
                // 计算边距处理
                linePathToHexagon = makeShape(lineRectF, canvas, linePaintToHexagon, false);
                // 生成 Bitmap
                bitmapToHexagon = Bitmap.createBitmap((int) (rectF.right - rectF.left), (int) (rectF.right - rectF.left), Bitmap.Config.ARGB_8888);
                // 生成新的 Canvas
                canvasToHexagon = new Canvas(bitmapToHexagon);
                // 计算中心点
                centerToHexagon = ((rectF.right - rectF.left) / 2);
                break;
            case Annulus: // 环形
                // 环形不需要绘制计算(初始化)
                break;
        }
    }

    /**
     * 绘制动画相关处理
     * @param canvas
     */
    private void drawAnim(Canvas canvas){
        try {
            // 位置信息
            float r; // 半径
            float mX; // X 轴中心点位置
            float mY; // Y 轴中心点位置
            // 获取扫描区域大小
            RectF rectF;
            // 判断形状类型
            switch (shapeType) {
                case Square: // 正方形
                    // 如果 bitmap 不为null, 才处理
                    if (bitmapToSquare != null){
                        // 获取扫描区域大小(正方形在内部绘制, 不需要加上外边距)
                        rectF = calcShapeRegion();
                        // 计算边距处理
                        rectF.left = rectF.left + lineMarginLeftToSquare;
                        rectF.top = rectF.top + lineMarginTopToSquare;
                        rectF.right = rectF.right - lineMarginLeftToSquare;
                        rectF.bottom = rectF.bottom - lineMarginTopToSquare;
                        // 循环划线，从上到下
                        if (lineOffsetToSquare > rectF.bottom - rectF.top - dfCommonDP) {
                            lineOffsetToSquare = 0;
                        } else {
                            lineOffsetToSquare = lineOffsetToSquare + 6;
                            // 设置线条区域
                            Rect lineRect = new Rect();
                            lineRect.left = (int) rectF.left;
                            lineRect.top = (int) (rectF.top + lineOffsetToSquare);
                            lineRect.right = (int) rectF.right;
                            lineRect.bottom = (int) (rectF.top + dfCommonDP + lineOffsetToSquare);
                            canvas.drawBitmap(bitmapToSquare, null, lineRect, linePaintToSquare);
                        }
                    }
                    break;
                case Hexagon: // 六边形
                    // 获取扫描区域大小
                    rectF = calcShapeRegion(lineMarginToHexagon);
                    // 位置信息
                    r = (rectF.right - rectF.left) / 2; // 半径
//                    mX = (rectF.right + rectF.left) / 2; // X 轴中心点位置
                    mY = (rectF.top + rectF.bottom) / 2; // Y 轴中心点位置
                    // 绘制线条
                    canvas.drawBitmap(bitmapToHexagon, rectF.left, mY - r, null);
                    break;
                case Annulus: // 环形
                    // 动画运行中才处理
                    if (isAnimRunning()){
                        if (bitmapToAnnulus != null){
                            float margin = - (annulusWidths[0] + annulusWidths[1] + annulusWidths[2] + annulusMargins[0]);
                            // 获取扫描区域大小
                            rectF = calcShapeRegion(margin);
                            // 位置信息
                            r = (rectF.right - rectF.left) / 2; // 半径
                            mX = (rectF.right + rectF.left) / 2; // X 轴中心点位置
                            mY = (rectF.top + rectF.bottom) / 2; // Y 轴中心点位置
                            // =
                            lineOffsetToAnnulus += lineOffsetSpeedToAnnulus;
                            if (lineOffsetToAnnulus > r * 2 - (annulusWidths[2])) {
                                lineOffsetToAnnulus = 0;
                            }
                            float p1, p2, hw;
                            if (lineOffsetToAnnulus >= r) {
                                p1 = (lineOffsetToAnnulus - r) * (lineOffsetToAnnulus - r);
                            } else {
                                p1 = (r - lineOffsetToAnnulus) * (r - lineOffsetToAnnulus);
                            }
                            p2 = r * r;
                            hw = (int) Math.sqrt(p2 - p1) - annulusMargins[2];

                            // 获取图片高度
                            int bitmapHeight = bitmapToAnnulus.getHeight();

                            RectF lineRectF = new RectF();
                            lineRectF.left = mX - hw;
                            lineRectF.top = rectF.top + lineOffsetToAnnulus;
                            lineRectF.right = mX + hw;
                            lineRectF.bottom = rectF.top + lineOffsetToAnnulus + bitmapHeight;
                            canvas.drawBitmap(bitmapToAnnulus, null, lineRectF, null);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "drawAnim - " + shapeType.name());
        }
    }

    // == 动画相关 ==

    // == 内部处理 ==

    /**
     * 重新设置动画
     * @param init
     */
    private void resetAnim(boolean init){
        // 是否重新初始化
        if (init){
            // 停止动画
            stopAnim();
            // 重置动画处理
            initAnim();
        }
        // 表示需要重新处理
        isReAnim = true;
    }

    // ==

    // 动画操作
    private final int START_ANIM = 10; // 开始动画
    private final int STOP_ANIM = 11; // 停止动画
//    private final int PAUSE_ANIM = 12; // 暂停动画

    /**
     * 启动动画
     */
    public void startAnim(){
        // 已经在运行了, 则不处理
        if (isAnimRunning()){
            return;
        }
        animSwitch(START_ANIM);
    }

    /**
     * 停止动画
     */
    public void stopAnim(){
        animSwitch(STOP_ANIM);
    }

//    /**
//     * 暂停动画
//     */
//    public void pauseAnim(){
//        animSwitch(PAUSE_ANIM);
//    }

    /**
     * 动画开关统一方法
     * @param operate
     */
    private void animSwitch(int operate){
        try {
            // 动画对象
            ValueAnimator valueAnimator = null;
            // 判断形状类型
            switch (shapeType) {
                case Square: // 正方形
                    valueAnimator = animToSquare;
                    break;
                case Hexagon: // 六边形
                    valueAnimator = animToHexagon;
                    break;
                case Annulus: // 环形
                    valueAnimator = animToAnnulus;
                    break;
            }
            if (valueAnimator != null){
                switch (operate){
                    case START_ANIM:
                        if (valueAnimator.isPaused()){
                            valueAnimator.resume();
                        } else {
                            valueAnimator.start();
                        }
                        break;
                    case STOP_ANIM:
                        valueAnimator.cancel();
                        break;
//                    case PAUSE_ANIM:
//                        valueAnimator.pause();
//                        break;
                }
            }
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "animSwitch - " + shapeType.name());
        }
    }

    /**
     * 是否动画运行中
     * @return
     */
    public boolean isAnimRunning(){
        try {
            // 判断形状类型
            switch (shapeType) {
                case Square: // 正方形
                    if (animToSquare != null){
                        return animToSquare.isRunning();
                    }
                    break;
                case Hexagon: // 六边形
                    if (animToHexagon != null){
                        return animToHexagon.isRunning();
                    }
                    break;
                case Annulus: // 环形
                    if (animToAnnulus != null){
                        return animToAnnulus.isRunning();
                    }
                    break;
            }
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "isAniming - " + shapeType.name());
        }
        return false;
    }

    // ======

    // == 正方形动画参数 ==

    /**
     * 重置线条颜色(进行着色)
     */
    private void refLineColorToSquare(){
        if (bitmapToSquare != null && lineColorToSquare != 0){
            try {
                // 转换Drawable
                Drawable drawable = new BitmapDrawable(getContext().getResources(), bitmapToSquare);
                Drawable tintDrawable = DrawableCompat.wrap(drawable);
                // 进行着色
                DrawableCompat.setTint(tintDrawable, lineColorToSquare);
                // 保存着色后的 Bitmap
//            bitmapToSquare = ((BitmapDrawable) tintDrawable).getBitmap();
                // 临时 Bitmap
                Bitmap bitmap;
                // 创建新的 Bitmap
                if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                    bitmap = Bitmap.createBitmap(1, 1,
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
                } else {
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
                }
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                // 保存着色后的 Bitmap
                bitmapToSquare = bitmap;
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "refLineColorToSquare");
            }
        }
    }

    // == 六边形动画参数 ==
    private float startLinePoint; // 起点位置
    private float endLinePoint; // 结束点位置
    private float offsetLinePoint; // 移动点位置
    // 线条颜色数组(渐变)
    private int[] lineColorArray;
    // 线条移动位置数组
    private float[] linePathArray;
    // 线条颜色
    private @ColorInt int lineColorToHexagon = Color.WHITE;
    // 线条 rgb 色值
    private int lineRed, lineGreen, lineBlue;
    // 透明度 0, 透明度 255 对应的颜色
    private @ColorInt int lineTran00Color, lineTran255Color;
    // ====================

    /**
     * 刷新线条颜色
     * 每次设置颜色值, 需要同步更新
     */
    private void refLineColorToHexagon(){
        // 获取红色 - 色值
        lineRed = Color.red(lineColorToHexagon);
        // 获取绿色 - 色值
        lineGreen = Color.green(lineColorToHexagon);
        // 获取蓝色 - 色值
        lineBlue = Color.blue(lineColorToHexagon);
        // 透明度 0 线条
        lineTran00Color = Color.argb(0, lineRed, lineGreen, lineBlue);
        lineTran255Color = Color.argb(255, lineRed, lineGreen, lineBlue);
    }

    // == 环形动画参数 ==

    /**
     * 重置线条颜色(进行着色)
     */
    private void refLineColorToAnnulus(){
        if (bitmapToAnnulus != null && lineColorToAnnulus != 0){
            try {
                // 转换Drawable
                Drawable drawable = new BitmapDrawable(getContext().getResources(), bitmapToAnnulus);
                Drawable tintDrawable = DrawableCompat.wrap(drawable);
                // 进行着色
                DrawableCompat.setTint(tintDrawable, lineColorToAnnulus);
                // 保存着色后的 Bitmap
//            bitmapToAnnulus = ((BitmapDrawable) tintDrawable).getBitmap();
                // 临时 Bitmap
                Bitmap bitmap;
                // 创建新的 Bitmap
                if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                    bitmap = Bitmap.createBitmap(1, 1,
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
                } else {
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
                }
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                // 保存着色后的 Bitmap
                bitmapToAnnulus = bitmap;
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "refLineColorToAnnulus");
            }
        }
    }

    // == 动画初始化 ==

    /**
     * 初始化动画
     */
    private void initAnim() {
        // 判断是否绘制动画
        if (!isDrawAnim){
            return;
        }
        // 判断形状类型
        switch (shapeType) {
            case Square: // 正方形
                animToSquare = ValueAnimator.ofInt(10, 20);
                animToSquare.setDuration(lineDurationToSquare);
                animToSquare.setRepeatCount(ValueAnimator.INFINITE);
                animToSquare.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        super.onAnimationRepeat(animation);
                        // 绘制
                        postInvalidate();
                    }
                });
                break;
            case Hexagon: // 六边形
                // 刷新颜色值
                refLineColorToHexagon();
                // 360, 0 从左到右, 0, 360 从右到左
                animToHexagon = ValueAnimator.ofInt(360, 0); // 暂时不修改该方法, 在内部更新方法写逻辑计算
                animToHexagon.setDuration(5 * 360);
                animToHexagon.setRepeatCount(ValueAnimator.INFINITE);
                animToHexagon.setInterpolator(new TimeInterpolator() {
                    @Override
                    public float getInterpolation(float input) {
                        return input;
                    }
                });
                animToHexagon.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (canvasToHexagon == null) {
                            return;
                        }
                        // 获取递减值 => ofInt 360 递减
                        Integer value = (Integer) animation.getAnimatedValue();
                        // 从左往右动画
                        if (lineAnimDirection) {
                            startLinePoint = value / 360f;
                            if (startLinePoint >= 0.25f) {
                                startLinePoint = startLinePoint - 0.25f;
                            } else {
                                startLinePoint = startLinePoint + 0.75f;
                            }
                            // 计算结束点的位置
                            endLinePoint = startLinePoint + 0.5f;
                            if (startLinePoint > 0.5f) {
                                // 计算移动距离, 对应的透明度
                                offsetLinePoint = startLinePoint - 0.5f;
                                // 转换 argb
                                int splitColor = Color.argb((int) (255 * (offsetLinePoint / 0.5f)), lineRed, lineGreen, lineBlue);
                                // 设置线条颜色
                                lineColorArray = new int[] { splitColor, lineTran00Color, 0, 0, lineTran255Color, splitColor };
                                // 设置线条动画路径
                                linePathArray = new float[] { 0f, offsetLinePoint, offsetLinePoint, startLinePoint, startLinePoint, 1f };
                            } else {
                                // 设置线条颜色
                                lineColorArray = new int[] { 0, 0, lineTran255Color, lineTran00Color, 0, 0 };
                                // 设置线条动画路径
                                linePathArray = new float[] { 0f, startLinePoint, startLinePoint, endLinePoint, endLinePoint, 1f };
                            }
                        } else { // 从右向左动画
                            startLinePoint = (360 - value) / 360f;
                            if (startLinePoint >= 0.25f) {
                                startLinePoint = startLinePoint - 0.25f;
                            } else {
                                startLinePoint = startLinePoint + 0.75f;
                            }
                            // 计算结束点的位置
                            endLinePoint = startLinePoint + 0.5f;
                            if (startLinePoint > 0.5f) {
                                // 计算移动距离, 对应的透明度
                                offsetLinePoint = startLinePoint - 0.5f;
                                // 转换 argb
                                int splitColor = Color.argb((int) (255 * (offsetLinePoint / 0.5f)), lineRed, lineGreen, lineBlue);
                                // 设置线条颜色
                                lineColorArray = new int[] { splitColor, lineTran00Color, 0, 0, lineTran255Color, splitColor };
                                // 设置线条动画路径
                                linePathArray = new float[] { 0f, offsetLinePoint, offsetLinePoint, startLinePoint, startLinePoint, 1f };
                            } else {
                                // 设置线条颜色
                                lineColorArray = new int[] { 0, 0, lineTran255Color, lineTran00Color, 0, 0 };
                                // 设置线条动画路径
                                linePathArray = new float[] { 0f, startLinePoint, startLinePoint, endLinePoint, endLinePoint, 1f };
                            }
                        }
                        // ======
                        // 绘制线条渐变效果
                        SweepGradient mShader = new SweepGradient(centerToHexagon, centerToHexagon, lineColorArray, linePathArray);
                        linePaintToHexagon.setShader(mShader);
                        canvasToHexagon.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvasToHexagon.drawPath(linePathToHexagon, linePaintToHexagon);
                        // 绘制
                        postInvalidate();
                    }
                });
                break;
            case Annulus: // 环形
                animToAnnulus = ValueAnimator.ofInt(10, 20);
                animToAnnulus.setDuration(1l);
                animToAnnulus.setRepeatCount(ValueAnimator.INFINITE);
                animToAnnulus.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        super.onAnimationRepeat(animation);
                        // 动画计算旋转
                        annulusAngles[0] += 4;
                        annulusAngles[1] += 2;
                        if (isOffsetMaxToAnnulus) {
                            if (annulusAngles[2] == 30) {
                                isOffsetMaxToAnnulus = false;
                            } else {
                                annulusAngles[2] ++;
                            }
                        } else {
                            if (annulusAngles[2] == -30) {
                                isOffsetMaxToAnnulus = true;
                            } else {
                                annulusAngles[2] --;
                            }
                        }
                        // 绘制
                        postInvalidate();
                    }
                });
                break;
        }
    }
}