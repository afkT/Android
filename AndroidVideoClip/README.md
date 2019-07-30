# [AndroidVideoClip](https://github.com/afkT/Android/tree/master/AndroidVideoClip)

Android 视频裁剪 (含裁剪 View)


### 具体代码

- [VideoClip](https://github.com/afkT/Java/tree/master/VideoClip)

- [PlaySeekbar](https://github.com/afkT/Android/tree/master/PlaySeekbar)


### 使用方法

```java
// -- am_video_seekbar.getStartTime() 获取的是自定义 View 选择的开始裁剪时间
// -- am_video_seekbar.getEndTime() 获取的是自定义 View 选择的结束裁剪时间
public void initListener(){
    // 点击视频裁剪
    am_cut_igview.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 禁止点击
            am_cut_igview.setEnabled(false);
            // --
            TrimVideoUtils trimVideoUtils = TrimVideoUtils.getInstance();
            trimVideoUtils.setTrimCallBack(new TrimVideoUtils.TrimFileCallBack() {
                @Override
                public void trimError(int eType) {
                    Message msg = new Message();
                    msg.what = TrimVideoUtils.TRIM_FAIL;
                    switch(eType){
                        case TrimVideoUtils.FILE_NOT_EXISTS: // 文件不存在
                            msg.obj = "视频文件不存在";
                            break;
                        case TrimVideoUtils.TRIM_STOP: // 手动停止裁剪
                            msg.obj = "停止裁剪";
                            break;
                        case TrimVideoUtils.TRIM_FAIL:
                        default: // 裁剪失败
                            msg.obj = "裁剪失败";
                            break;
                    }
                    cutHandler.sendMessage(msg);
                }
                @Override
                public void trimCallback(boolean isNew, int startS, int endS, int vTotal, File file, File trimFile) {
                    /**
                     * 裁剪回调
                     * @param isNew 是否新剪辑
                     * @param starts 开始时间(秒)
                     * @param ends 结束时间(秒)
                     * @param vTime 视频长度
                     * @param file 需要裁剪的文件路径
                     * @param trimFile 裁剪后保存的文件路径
                     */
                    // ===========
                    System.out.println("isNew : " + isNew);
                    System.out.println("startS : " + startS);
                    System.out.println("endS : " + endS);
                    System.out.println("vTotal : " + vTotal);
                    System.out.println("file : " + file.getAbsolutePath());
                    System.out.println("trimFile : " + trimFile.getAbsolutePath());
                    // --
                    cutHandler.sendEmptyMessage(TrimVideoUtils.TRIM_SUCCESS);
                }
            });
            // 需要裁剪的视频路径
            String videoPath = PLAY_URL;
            // 保存的路径
            String savePath = ProUtils.getSDCartPath() + File.separator  + System.currentTimeMillis() + "_cut.mp4";
            // ==
            final File file = new File(videoPath); // 视频文件地址
            final File trimFile = new File(savePath);// 裁剪文件保存地址
            // 获取开始时间
            final int startS = (int) am_video_seekbar.getStartTime() / 1000;
            // 获取结束时间
            final int endS = (int) am_video_seekbar.getEndTime() / 1000;
            // 进行裁剪
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try { // 开始裁剪
                        TrimVideoUtils.getInstance().startTrim(true, startS, endS, file, trimFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 设置回调为null
                        TrimVideoUtils.getInstance().setTrimCallBack(null);
                    }
                }
            }).start();
            // --
            Toast.makeText(MainActivity.this, "开始裁剪 - 开始: " + startS  + "秒, 结束: " + endS + "秒", Toast.LENGTH_SHORT).show();
        }
    });
}
```

代码中点击裁剪后，只是禁止按钮点击没有显示加载 Dialog，自己根据项目需求搬运过去，以及计算关键帧的方法需要放在后台线程内进行计算，防止卡顿

```java
// ==========================================
// 计算关键帧可能会卡顿一下, 最好是在后台运行
// ==========================================

// 获取视频关键帧间隔 - 如果获取失败, 则默认最少需要裁剪 3 秒长度的视频
float videoFrame = (float) TrimVideoUtils.getInstance().reckonFrameTime(new File(videoUrl), 3000);
// 设置本地视频路径 - 默认裁剪模式, 则不绘制播放背景
am_video_seekbar.setVideoUri(true, PLAY_URL, videoFrame);
// --
Toast.makeText(MainActivity.this, "视频关键帧：" + videoFrame, Toast.LENGTH_SHORT).show();
```

### 注意事项

> 计算关键帧的作用 - 裁剪开始时间 - 结束时间，中间的空白模块就是 videoFrame，你可以限制最低裁剪多少秒，会自动计算视频总时间，以及 View 的宽度换算 X 轴对应的时间