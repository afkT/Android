# AndroidScreenMatch
Android 屏幕适配计算生成对应的尺寸文件 - IntelliJ IDEA Java Pro


### 具体参考:

> https://github.com/mengzhinan/PhoneScreenMatch

> https://blog.csdn.net/lmj623565791/article/details/45460089

### 默认生成存储到该项目 res目录下, 可以自行更改

> GeneratePxValueFiles
> </br>生成对应的 x,y适配文件
> </br>values-HeightxWidth => values-1334x750


> GenerateDPValueFiles
> </br>需要先在该项目目录下 res/values 文件夹内 创建适配的 dimens.xml 文件, 并且修改基准 DP
> </br>然后自动生成对应的 
> </br>values-wXXXdp => values-w820dp

### 获取手机尺寸、DP等

> http://screensiz.es/droid-razr