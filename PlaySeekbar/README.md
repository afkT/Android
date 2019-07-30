# [PlaySeekbar](https://github.com/afkT/Android/tree/master/PlaySeekbar)

视频裁剪自定义 View


### 功能需求与预览

- 有个视频裁剪功能，需要自定义 View 具体如下

1. 裁剪选择区域模块，可以自定义最少裁剪时间

2. 当选择低于最少裁剪时间时，再次滑动会自动推动左右选择轴，直至碰到边缘为止

3. 选择空白区域，并左右滑动时自动推动选择轴的位置

4. 可选控制(是否裁剪模式，是否显示播放进度，裁剪模式下(未选中的背景增加阴影图层)，播放过的背景增加阴影图层等)

![gif](https://raw.githubusercontent.com/afkT/Android/master/PlaySeekbar/mdFile/img1.gif)


### 具体实现与使用

- [VideoSeekBar.java](https://github.com/afkT/Android/blob/master/PlaySeekbar/app/src/main/java/com/play/pro/widgets/VideoSeekBar.java)


### 使用方法：

> 跑 Demo 前需要找个视频, 并且在 MainActivity 设置本地视频地址

```java
public void initValues(){
	// 初始化播放控制器
	playerControl = new PlayerControl(this, vHandler);
	// 根目录
	String rootPath = ProUtils.getSDCartPath();
	// 本地视频
	String videoUrl = rootPath + "/a.mp4";
	// --
	// videoUrl = rootPath + "/b.mp4";
	// 重新赋值地址
	PLAY_URL = videoUrl;
	// 加载视频封面
	playerControl.initLoad(COVER_URL, false);
	// 开始播放
	vHandler.sendEmptyMessage(FinalConstants.PLAY_START);
	
	// ====== 视频剪辑View  ======
	// 进行重置
	am_video_seekbar.reset();
//	// 是否需要绘制进度 - 白色进度动,以及走过的画面背景变暗 - 统一控制setProgressLine(isDrawProgress), setProgressBG(isDrawProgress)
//	am_video_seekbar.setProgressDraw(isDrawProgress);
//	//// 是否需要绘制进度 - 播放中,有个白色的线条在动
//	am_video_seekbar.setProgressLine(isDrawProgressLine);
//	// 是否需要绘制进度 - 播放过的画面背景变暗
//	am_video_seekbar.setProgressBG(isDrawProgressBG);
//	// 是否属于裁剪模式 - 两边有进度滑动
//	am_video_seekbar.setCutMode(isCutMode);
//	// 是否属于裁剪模式 - 是否绘制非裁剪模块变暗
//	am_video_seekbar.setCutMode(isCutMode, isDrawProgressLine);
	// 视频关键帧间隔(毫秒,表示左右两个模块最低限度滑动时间,无法选择低于该关键帧的裁剪时间)
	float videoFrame = 60 * 1000f;
	// 设置本地视频路径 - 默认裁剪模式,则不绘制播放背景
	am_video_seekbar.setVideoUri(true, PLAY_URL, videoFrame);
//	// 不设置关键帧时间,则默认最多是两个ImageView左右多出的宽度
//	am_video_seekbar.setVideoUri(isCutMode, videoUri);
}
```