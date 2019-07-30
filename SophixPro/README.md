# [SophixPro](https://github.com/afkT/Android/tree/master/SophixPro)

Android - 热修复 Sophix


### Gradle

```
allprojects {
    repositories {
        google()
        jcenter()

        // 热修复
        maven {
            url 'http://maven.aliyun.com/nexus/content/repositories/releases/'
        }
    }
}

dependencies {
    // 分包
    compile 'com.android.support:multidex:1.0.1'
    // 阿里移动热修复
    compile 'com.aliyun.ams:alicloud-android-hotfix:3.2.3'
}
```

### 快速集成

- [快速集成](https://help.aliyun.com/document_detail/61082.html?spm=a2c4g.11186623.6.560.7fe65c56uYoJfS)

### 方案对比

![img](https://raw.githubusercontent.com/afkT/Android/master/SophixPro/mdFile/img1.png)


### 注意事项

- 每次热修复，是和发布的版本对比，而不是已经第 xx 次后的修复对比

- 可以比作是，每次都当做新的补丁

- 所以每次提交，都需要存在之前修复 bug 的代码