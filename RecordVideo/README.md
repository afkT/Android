# RecordVideo

Android - 录制小视频 View Demo（拍照 + 视频）支持前、后置，录制拉近以及横竖屏翻转处理

### 预览

<img src="https://raw.githubusercontent.com/afkT/Android/master/RecordVideo/mdFile/img1.png" width="360"/>&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/afkT/Android/master/RecordVideo/mdFile/img2.png" width="360"/>

<img src="https://raw.githubusercontent.com/afkT/Android/master/RecordVideo/mdFile/img3.png" width="360"/>&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/afkT/Android/master/RecordVideo/mdFile/img4.png" width="360"/>


### 类

* [MediaRecorderView](https://github.com/afkT/Android/blob/master/RecordVideo/app/src/main/java/com/record/video/widget/MediaRecorderView.java), 主要是显示Camera预览画面，以及录制代码、拍照代码，包括横竖屏、屏幕翻转等逻辑代码

* [MediaDealUtils](https://github.com/afkT/Android/blob/master/RecordVideo/app/src/main/java/com/record/video/utils/MediaDealUtils.java), 是对录制视频后缩略图生成、缩放处理

* [CameraUtils](https://github.com/afkT/Android/blob/master/RecordVideo/app/src/main/java/com/record/video/utils/CameraUtils.java), 摄像头资源操作

* [MediaRecordActivity](https://github.com/afkT/Android/blob/master/RecordVideo/app/src/main/java/com/record/video/activitys/MediaRecordActivity.java) 录制、拍照页面，[MediaResultPreActivity](https://github.com/afkT/Android/blob/master/RecordVideo/app/src/main/java/com/record/video/activitys/MediaResultPreActivity.java) 录制、拍照结果预览页面

### 注意实现

录制视频, 如果手机支持 480x640、360x640, 则默认使用该分辨率录制, 可在 MediaRecorderView 中进行修改判断

```java
/**
 * 设置配置大小(不同比例控制不同)
 * @param params
 */
private void setConfigSize(Camera.Parameters params){
  // 计算预览大小
  setPreviewSize(params);
  // 计算录制大小
  setVideoSize(params);
}
```

预览默认使用最符合屏幕分辨率的大小, 拍照相同

### 存在的缺陷：

该 Demo 使用的是 MediaRecorder 进行录制视频

并且该效果要求 点击拍照, 长按超过 0.x 秒则实现为录制, 而录制视频的时候需要切换 Camera 配置，会导致录制一瞬间导致卡顿

```java
// 拍照的时候, 不能调用该方法, 但是录制视频必须调用该方法, 导致需要重新初始化 Camera, 并重新配置参数
	
initCamera(){ // 该方法中
	// 导致无法拍照
	mCamera.unlock();
}
	
// 具体实现代码, 看 MediaRecorderView 类处理了翻转对应视频、图片旋转角度摆正，并且支持摄像头手势上下滑动，缩放摄像头
```