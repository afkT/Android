# [RecordView](https://github.com/afkT/Android/tree/master/RecordView)

录制进步式 View


### 功能需求与预览

- 项目有个直播功能，需要显示进度条类似录制进度 View 具体如下

1.可以控制顶部时间间隔，以及是否绘制，是否预留位置

2.能够重头开始，从指定时间开始、恢复、暂停、停止等

![gif](https://raw.githubusercontent.com/afkT/Android/master/RecordView/mdFile/img1.gif)

### 具体实现类

- [RecordTimeView.java](https://github.com/afkT/Android/blob/master/RecordView/app/src/main/java/com/pro/record/widgets/RecordTimeView.java)

### 使用方法

```java
/** 时间进度View */
private RecordTimeView am_rtview;

am_rtview.setKeepText(false); // 是否保留绘制文本的位置 - 如果进行属于进行绘制，则无视该参数
am_rtview.setDrawText(true); // 是否绘制文本
am_rtview.setTextSize(14.0f); // 设置文本大小 - 默认13f

am_rtview.start(); // 开始
//am_rtview.start(20 * 1000f); // 从指定时间开始
am_rtview.stop();// 停止录制
am_rtview.pause(); // 暂停
am_rtview.recover(); // 恢复
am_rtview.getTime(); // 获取时间，单位毫秒
am_rtview.getTimes(); // 获取时间，单位秒数

/** 设置时间触发回调 */
am_rtview.setRecordTimeCallBack(new RecordTimeView.RecordTimeCallBack() {
     @Override
     public void preSecond(float dTime) { // 满一秒触发
     }
     @Override
     public void start(float dTime) { // 开始、恢复时触发
     }
});
```