# [ScanView](https://github.com/afkT/Android/tree/master/ScanView)

扫描 (二维码/AR) 效果自定义 View


### 预览

| ![gif1](https://raw.githubusercontent.com/afkT/Android/master/ScanView/mdFile/1.gif) | ![gif2](https://raw.githubusercontent.com/afkT/Android/master/ScanView/mdFile/2.gif) | ![gif3](https://raw.githubusercontent.com/afkT/Android/master/ScanView/mdFile/3.gif) |
|:-|:-|:-|


### 具体实现

- [ScanShapeView.java](https://github.com/afkT/Android/blob/master/ScanView/app/src/main/java/ttt/scan/widgets/ScanShapeView.java)


### 使用

- [ScanActivity.java](https://github.com/afkT/Android/blob/master/ScanView/app/src/main/java/ttt/scan/ScanActivity.java)

```java
/**
 * 刷新类型处理
 */
private void refShape() {
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
//            // 设置 正方形描边(边框), 类型 0 = 单独四个角落, 1 = 单独边框, 2 = 全部
//            vid_as_scan.setBorderToSquare(2);
            break;
        case Hexagon: // 六边形
            // 白色
            int hexagonColor = Color.WHITE;
            // 边框颜色
            vid_as_scan.setBorderColor(hexagonColor);
            // 设置六边形线条动画颜色
            vid_as_scan.setLineColorToHexagon(hexagonColor);
//            // 设置六边形线条动画方向 true = 从左到右, false = 从右到左
//            vid_as_scan.setLineAnimDirection(false);
            break;
        case Annulus: // 环形
            // 设置环形线条动画(着色)
            vid_as_scan.setLineColorToAnnulus(Color.RED);
            // 设置是否需要阴影背景
            vid_as_scan.setDrawBack(false);
//            // 设置环形扫描线条速度
//            vid_as_scan.setLineOffsetSpeedToAnnulus(6f);
            break;
    }
    // 重新绘制
    vid_as_scan.postInvalidate();
}
```
