# [AndroidScreenMatch](https://github.com/afkT/Android/tree/master/AndroidScreenMatch)

Android 屏幕适配生成对应的尺寸文件 - IntelliJ IDEA Java Pro


### 具体参考

- [https://github.com/mengzhinan/PhoneScreenMatch](https://github.com/mengzhinan/PhoneScreenMatch)

- [https://blog.csdn.net/lmj623565791/article/details/45460089](https://blog.csdn.net/lmj623565791/article/details/45460089)


### 默认生成存储到该项目 res 目录下, 可以自行更改

- [GeneratePxValueFiles](https://github.com/afkT/Android/blob/master/AndroidScreenMatch/src/px/GeneratePxValueFiles.java) - 生成对应的 x, y 适配文件

> values-HeightxWidth -> values-1334x750


- [GenerateDPValueFiles](https://github.com/afkT/Android/blob/master/AndroidScreenMatch/src/dp/GenerateDPValueFiles.java) - 需要先在该项目目录下 res/values 文件夹内创建适配的 dimens.xml 文件, 并且修改基准 DP

> values-wXXXdp => values-w820dp


### 获取手机尺寸、DP 等

- [http://screensiz.es/droid-razr](http://screensiz.es/droid-razr)