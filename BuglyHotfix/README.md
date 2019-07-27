# BuglyHotfix

Android 热修复 - Bugly


### Gradle

```
dependencies {
    compile "com.android.support:multidex:1.0.1" // 多dex配置
    // 注释掉原有bugly的仓库
    // compile 'com.tencent.bugly:crashreport:latest.release' // 其中latest.release指代最新版本号，也可以指定明确的版本号，例如2.3.2
    compile 'com.tencent.bugly:crashreport_upgrade:1.3.4' // https://blog.csdn.net/y505772146/article/details/78966676
    compile 'com.tencent.bugly:nativecrashreport:latest.release' // 其中latest.release指代最新版本号，也可以指定明确的版本号，例如2.2.0
}
```

### 使用方法

<a href="https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix/?v=20180521124306">热更新使用指南</a>

<a href="https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix-demo/?v=20180521124306">热更新使用详解</a>

<a href="https://github.com/BuglyDevTeam/Bugly-Android-Demo">官方 demo</a>

### 注意事项

``` java
// 需要注意的是, 假设我发了个包
// 然后这个包有bug, 这个时候以这个基准包, 生成了Tinker补丁, 然后发布
// 但是发现修复了第一个bug后, 导致出现了第二个bug
// 这个时候打的差异包, 必须还是最开始的基准包（发包的apk）, 这样才能直接修复没有更新过补丁的用户
// 以及修复，更新过一次补丁，导致二次bug的用户
// 以此类推，每次的基准包都是以发包的版本为基准包, 只是修复的差异包，一直包含最新的代码

// 反正步骤 如下, 只要基础包生成过一次后, 或者指定位置后, 只需要每次修复都是 buildTinkerPatchRelease 上传补丁就行
// https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix-demo/?v=20180521124306
// 每次先生成基准包 在 :app -> Tasks -> build -> assembleRelease 构建后，复制最新的地址替换 baseApkDir
// 接着进行修复bug, 修复好了后, 更改 tinkerId
// 并且进行 :app -> Tasks -> tinker-support -> buildTinkerPatchRelease 生成补丁包
```


> * 具体配置方法在 tinker-support.gradle 中

> * https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix-demo/?v=20180521124306

> * 每次先生成基准包 在 :app -> Tasks -> build -> assembleRelease 构建后，复制最新的地址替换 baseApkDir

> * 接着进行修复bug, 修复好了后, 更改 tinkerId

> * 并且进行 :app -> Tasks -> tinker-support -> buildTinkerPatchRelease 生成补丁包