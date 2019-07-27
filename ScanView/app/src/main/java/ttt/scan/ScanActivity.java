package ttt.scan;

import android.Manifest;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.DevUtils;
import dev.utils.app.CameraUtils;
import dev.utils.app.PermissionUtils;
import dev.utils.app.SizeUtils;
import dev.utils.app.assist.camera.CameraAssist;
import dev.utils.app.image.BitmapUtils;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.toast.cus.Toasty;
import ttt.scan.widgets.ScanShapeView;

/**
 * detail: 扫描显示View
 * Created by Ttt
 */
public class ScanActivity extends AppCompatActivity {

    // 日志 TAG
    private final String TAG = ScanActivity.class.getSimpleName();

    // == View ==
    @BindView(R.id.vid_as_surface)
    SurfaceView vid_as_surface;
    @BindView(R.id.vid_as_scan)
    ScanShapeView vid_as_scan;
    // == Obj ==
    // 摄像头辅助类
    CameraAssist cameraAssist = new CameraAssist();
    // 获取类型 (默认正方形)
    ScanShapeView.Shape scanShape = ScanShapeView.Shape.Square;
    // 判断是否长按
    boolean isLongClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);
        // ===
//        // 设置特殊处理(查看阴影)
//        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) vid_as_scan.getLayoutParams();
//        layoutParams.height = 700;
//        layoutParams.width = 700;
        // 检查扫描类型
        checkShape();

        // 添加回调
        vid_as_surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // 检查权限
                checkPermission();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                try {
                    if (cameraAssist != null){
                        cameraAssist.stopPreview();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        // ==
        // 点击切换动画状态
        vid_as_surface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLongClick) {
                    isLongClick = false;
                    return;
                }
                // ============
                if (vid_as_scan.isAnimRunning()){
                     vid_as_scan.stopAnim();
                } else {
                    vid_as_scan.startAnim();
                }
                vid_as_scan.postInvalidate();
            }
        });

        // 长按切换类型
        vid_as_surface.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isLongClick = true;
                try {
                    scanShape = ScanShapeView.Shape.values()[scanShape.ordinal() + 1];
                } catch (Exception e){ // 出现异常表示需要重置第一个类型
                    scanShape = ScanShapeView.Shape.Square;
                }
                // 刷新类型
                refShape();
                return false;
            }
        });
    }

    /**
     * 检查摄像头权限
     */
    private void checkPermission(){
        // 摄像头权限
        String cameraPermission = Manifest.permission.CAMERA;
        // 判断是否允许权限
        if (PermissionUtils.isGranted(cameraPermission)){
            try {
                // 打开摄像头
                Camera camera = CameraUtils.open();
                camera.setDisplayOrientation(90);
                cameraAssist.setCamera(camera);
                // 开始预览
                cameraAssist.openDriver(vid_as_surface.getHolder()).startPreview();
                // 默认开启自动对焦, 设置不需要自动对焦
                 cameraAssist.setAutoFocus(false);
            } catch (Exception e){
                DevLogger.eTag(TAG, e, "checkPermission - startPreview");
            }
        } else {
            Toasty.info(this, "需要摄像头权限预览");
            // 申请权限
            PermissionUtils.permission(cameraPermission).callBack(new PermissionUtils.PermissionCallBack() {
                @Override
                public void onGranted(PermissionUtils permissionUtils) {
                    // 刷新处理
                    checkPermission();
                }

                @Override
                public void onDenied(PermissionUtils permissionUtils) {
                    // 再次申请权限
                    checkPermission();
                }
            }).request();
        }
    }

    // == 内部处理方法 ==

    /**
     * 检查扫描类型
     */
    private void checkShape(){
        // 判断类型
        String type = getIntent().getStringExtra("type");
        // 防止为null
        if (!TextUtils.isEmpty(type)){
            for (ScanShapeView.Shape shape : ScanShapeView.Shape.values()){
                if (shape.name().equals(type)){
                    scanShape = shape;
                    break;
                }
            }
        }
        // 刷新类型
        refShape();
    }

    /**
     * 刷新类型处理
     */
    private void refShape(){
        // 设置扫描 View 类型
        vid_as_scan.setShapeType(scanShape);

        boolean isExecute = false;
        if (isExecute) {
            // = 处理方法 =
            // 销毁方法
            vid_as_scan.destroy();
            // 启动动画
            vid_as_scan.startAnim();
            // 停止动画
            vid_as_scan.stopAnim();
            // 动画是否运行中
            vid_as_scan.isAnimRunning();

            // = 共用 =
            // 设置扫描 View 类型
            vid_as_scan.setShapeType(scanShape);
            // 获取扫描 View 类型
            vid_as_scan.getShapeType();
            // 设置是否绘制背景
            vid_as_scan.setDrawBack(true);
            // 设置背景颜色 - (黑色 百分之40透明度) #66000000
            vid_as_scan.setBackColor(Color.argb(102, 0, 0, 0));
            // 设置是否自动启动动画
            vid_as_scan.setAutoAnim(false);
            // 是否需要绘制动画(效果)
            vid_as_scan.setDrawAnim(false);
            // 设置拐角效果
            vid_as_scan.setCornerEffect(new ScanShapeView.CornerEffect(10));
            // 设置扫描区域大小(扫描View) 无关阴影背景以及整个View 宽高
            vid_as_scan.setRegion(700);
            vid_as_scan.setRegion(700, 700);
            vid_as_scan.setRegion(new Rect(0, 0, 700, 700));
            // 获取扫描区域 距离 整个View的左/右边距 距离
            vid_as_scan.getRegionLeft();
            // 获取扫描区域 距离 整个View的上/下边距 距离
            vid_as_scan.getRegionTop();
            // 获取扫描区域位置信息
            vid_as_scan.getRegion(); // 获取扫描区域位置信息
            vid_as_scan.getRegion(100f, 200f); // 获取纠偏(偏差)位置后的扫描区域
            vid_as_scan.getRegionParent(); // 获取扫描区域在View中的位置
            vid_as_scan.getRegionWidth();
            vid_as_scan.getRegionHeight();
            // 获取边框边距
            vid_as_scan.getBorderMargin();
            // 设置扫描区域绘制边框边距
            vid_as_scan.setBorderMargin(0);
            // 设置扫描区域边框颜色
            vid_as_scan.setBorderColor(Color.WHITE);
            // 设置扫描区域边框宽度
            vid_as_scan.setBorderWidth(SizeUtils.dipConvertPx(2));
            // 是否绘制边框
            vid_as_scan.setDrawBorder(true);

            // == 正方形特殊配置 ==
            // 设置 正方形描边(边框), 类型 0 = 单独四个角落, 1 = 单独边框, 2 = 全部
            vid_as_scan.setBorderToSquare(0);
            // 设置四个角落与边框共存时, 对应边框宽度
            vid_as_scan.setBorderWidthToSquare(SizeUtils.dipConvertPx(1));
            // 设置每个角的点距离(长度)
            vid_as_scan.setTriAngleLength(SizeUtils.dipConvertPx2(20));
            // 设置特殊处理(正方形边框) - 当 描边类型为 2 , 并且存在圆角时, 设置距离尺寸过大会出现边框圆角 + 四个角落圆角有部分透出背景情况
            vid_as_scan.setSpecialToSquare(false); // 出现的时候则设置 true, 小尺寸(setBorderWidthToSquare, setBorderWidth) 则不会出现
            // 设置正方形扫描动画速度(毫秒)
            vid_as_scan.setLineDurationToSquare(10l);
            // 设置正方形扫描线条 Bitmap
            vid_as_scan.setBitmapToSquare(BitmapUtils.getBitmapFromResources(DevUtils.getContext(), R.drawable.scanline));
            // 设置正方形线条动画(着色) -> 如果不使用自己的 Bitmap(setBitmapToSquare), 则可以使用默认内置的图片, 进行着色达到想要的颜色
            vid_as_scan.setLineColorToSquare(Color.WHITE);
            // 设置正方形扫描线条向上(下)边距
            vid_as_scan.setLineMarginTopToSquare(0);
            // 设置正方形扫描线条向左(右)边距
            vid_as_scan.setLineMarginLeftToSquare(0);

            // == 六边形特殊配置 ==
            // 设置六边形线条动画 - 线条宽度
            vid_as_scan.setLineWidthToHexagon(4f);
            // 置六边形线条动画 - 线条边距
            vid_as_scan.setLineMarginToHexagon(20f);
            // 设置六边形线条动画方向 true = 从左到右, false = 从右到左
            vid_as_scan.setLineAnimDirection(true);
            // 设置六边形线条动画颜色
            vid_as_scan.setLineColorToHexagon(Color.WHITE);

            // == 环形特殊配置 ==
            // 设置环形扫描线条 Bitmap
            vid_as_scan.setBitmapToAnnulus(BitmapUtils.getBitmapFromResources(DevUtils.getContext(), R.drawable.scanline));
            // 设置环形线条动画(着色)
            vid_as_scan.setLineColorToAnnulus(Color.WHITE);
            // 设置环形扫描线条速度
            vid_as_scan.setLineOffsetSpeedToAnnulus(4);
            // 设置环形对应的环是否绘制 0 - 外环, 1 - 中间环, 2 - 外环
            vid_as_scan.setAnnulusDraws(false, true, true);
            // 设置环形对应的环绘制颜色 0 - 外环, 1 - 中间环, 2 - 外环
            vid_as_scan.setAnnulusColors(Color.BLUE, Color.RED, Color.WHITE);
            // 设置环形对应的环绘制长度 0 - 外环, 1 - 中间环, 2 - 外环
            vid_as_scan.setAnnulusLengths(20, 30, 85);
            // 设置环形对应的环绘制宽度 0 - 外环, 1 - 中间环, 2 - 外环
            vid_as_scan.setAnnulusWidths(SizeUtils.dipConvertPx(3), SizeUtils.dipConvertPx(7), SizeUtils.dipConvertPx(7));
            // 设置环形对应的环绘制边距 0 - 外环, 1 - 中间环, 2 - 外环
            vid_as_scan.setAnnulusMargins(SizeUtils.dipConvertPx(7), SizeUtils.dipConvertPx(7), SizeUtils.dipConvertPx(7));
        }

        // 设置是否需要阴影背景
        vid_as_scan.setDrawBack(true);

        // 判断类型
        switch (scanShape){
            case Square: // 正方形
                // 天蓝色
                int squareColor = Color.argb(255, 0, 128, 255);
                // 设置扫描线条颜色
                vid_as_scan.setLineColorToSquare(squareColor);
                // 边框颜色
                vid_as_scan.setBorderColor(squareColor);
                // 不需要圆角
                vid_as_scan.setCornerEffect(null);
//                // 设置 正方形描边(边框), 类型 0 = 单独四个角落, 1 = 单独边框, 2 = 全部
//                vid_as_scan.setBorderToSquare(2);
                break;
            case Hexagon: // 六边形
                // 白色
                int hexagonColor = Color.WHITE;
                // 边框颜色
                vid_as_scan.setBorderColor(hexagonColor);
                // 设置六边形线条动画颜色
                vid_as_scan.setLineColorToHexagon(hexagonColor);
//                // 设置六边形线条动画方向 true = 从左到右, false = 从右到左
//                vid_as_scan.setLineAnimDirection(false);
                break;
            case Annulus: // 环形
                // 设置环形线条动画(着色)
                vid_as_scan.setLineColorToAnnulus(Color.RED);
                // 设置是否需要阴影背景
                vid_as_scan.setDrawBack(false);
//                // 设置环形扫描线条速度
//                vid_as_scan.setLineOffsetSpeedToAnnulus(6f);
                break;
        }
        // 重新绘制
        vid_as_scan.postInvalidate();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁处理
        vid_as_scan.destroy();
    }
}
