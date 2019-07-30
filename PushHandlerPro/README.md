# [PushHandlerPro](https://github.com/afkT/Android/tree/master/PushHandlerPro)

Android 点击推送逻辑处理、页面跳转判断


### 需求


>// 该项目主要实现此需求:
>
>// 收到推送, 点击推送消息处理
>// 1. 如果 应用已经打开, 这直接做处理, 如跳转页面, 打开链接等功能
>// 2. 如果 应用没有开启, 则默认先开启应用 (如果存在引导页面, 则显示引导页面), 然后进入首页后, 再做对应的处理(跳转页面, 打开链接等)
>
>// 同微信一样
>// 如果微信没有打开(后台被销毁), 有人发送消息过来, 点击通知栏, 则默认是打开微信, 显示欢迎页, 接着进入对应的群聊、或者私聊页面, 返回键则返回到消息列表页面
>// 如果微信已打开, 则不会再次启动微信, 直接进入对应的页面
>
>
>// == 效果 ==
>
>// 收到推送后, 并且点击推送
>
>// 没开启应用：
>
>// 打开应用 -> 默认显示 Launcher 页面( WelcomeActivity ) -> 接着进入 MainActivity -> 触发onResume方法, 判断成功后, 跳转到对应的 PushXxxActivity
>
>// 已经开启应用
>
>// 默认停留在 MainActivity 页面, 触发onResume方法, 判断成功后, 跳转到对应的 PushXxxActivity


### 主要功能实现页面

- [BaseApplication](https://github.com/afkT/Android/blob/master/PushHandlerPro/app/src/main/java/com/push/handler/BaseApplication.java) => onCreate 方法

- [BaseActivity](https://github.com/afkT/Android/blob/master/PushHandlerPro/app/src/main/java/com/push/handler/BaseActivity.java) => onResume 方法

- [PushHanderActivity](https://github.com/afkT/Android/blob/master/PushHandlerPro/app/src/main/java/com/push/handler/receiver/push/PushHanderActivity.java)


### 实现思路

>// == 实现思路 ==
>
>// 默认全部推送, 都跳转到统一一个页面 PushHanderActivity, 该页面不显示任何UI, 只是单独中转处理
>
>// 接着在 PushHanderActivity 中, 保存传递过来的推送消息、推送类型到 SharedPreferences (SP) 中, 并且默认跳转到应用 Launcher 页面 (startActivity(Launcher.class);)
>
>// 然后在全部 Activity 中的 onResume 中调用 PushHanderActivity.checkPush 方法, 主要就是通过获取 SP 存储的值, 判断是否存在推送消息
>
>// 不存在推送消息, 则不处理
>
>// 存在则处理, 并通过接口进行回调通知, => 会先通过 接口 isHandlerPush 方法判断是否处理推送, 如果处理, 并且存在数据则会调用 onPushHandler 接口方法
>
>// isHandlerPush 可以特殊处理, 正常传入的参数为 class getSimpleName 名, 容易判断是否不处理某些类
>
>
>
>// == 使用步骤 ==
>
>// 需要三步
>
>// 1.首先在 Application 中 调用 PushHanderActivity.setLauncherClass, 并且设置 Launcher 页面.class
>
>// 2.PushHanderActivity.setPushHandler 实现IPushHandler接口, 并且进行编写逻辑判断
>
>// 3.在整个项目 Activity基类 的onResume方法中, 调用 PushHanderActivity.checkPush(activity, value); // 正常 PushHanderActivity.checkPush(this, this.getClass().getSimpleName());
>
>
>
>// == 方法功能介绍 ==
>
>// PushHanderActivity.checkPush 该方法功能, 主要是判断是否存在推送数据, 以及对推送数据特殊处理
>
>// 需要在onResume中处理, 是因为如果应用已经打开, 并且停留在某个页面, 点击推送会再次触发onResume, 这个时候进行判断是否处理推送
>
>// ====
>
>// 而 isHandlerPush 方法, 则主要是判断是否在该页面处理推送消息, 正常是忽略欢迎、引导页面
>
>// 只有正常页面才进行处理, 防止影响到需要强制显示的页面(如欢迎页面, 会显示一定延迟时间后, 才处理推送)
>
>// onPushHandler 方法是自己逻辑处理，点击推送后做什么操作