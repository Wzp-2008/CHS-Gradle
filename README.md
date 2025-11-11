# Gradle 国人优化计划

### 本项目旨在为国内开发者提供一个舒适的Gradle使用环境

### 功能：

1. 使Gradle的src源码包从任意国内源（默认为腾讯云）下载
2. ~~暂未开发~~

### 使用

1. 下载CHS-Gradle.jar并放置到gradle用户目录（用户目录下.gradle文件夹内）
2. 在.gradle文件夹下打开gradle.properties文件（没有就创建一个）
3. 在最后写入：
    ```text
    org.gradle.jvmargs=-javaagent:[替换为你的CHS-Gradle.jar文件绝对路径]
    ``` 
4. 关闭gradle daemon进程

### 注意事项

1. 不支持snapshot版本Gradle（因为国内大部分镜像源没有镜像）
2. 对于wrapper安装加速无能为例（若需要加速可以看下方的使用建议部分 ）
3. 若需要更换gradle-src下载源可以通过设置环境变量gradle.src.base来进行修改
    ```text
    gradle.src.base=https://mirrors.cloud.tencent.com/gradle
    ```

### 使用建议

1. 若需要让gradle项目的依赖/插件使用国内源下载可以使用如下脚本：

    ````groovy
    //阿里云gradle插件镜像仓库地址
    final PLUGIN_SOURCE = "https://maven.aliyun.com/repository/gradle-plugin"
    //阿里云maven镜像仓库地址
    final PUBLIC_REPOSITORY = "https://maven.aliyun.com/repository/public"
    gradle.settingsEvaluated { settings ->
        settings.pluginManagement.repositories {
            maven { url PLUGIN_SOURCE }
            gradlePluginPortal()
            mavenCentral()
        }
    }
    allprojects { project ->
        repositories {
            maven { url PUBLIC_REPOSITORY }
            google()
            mavenCentral()
        }
        buildscript {
            repositories {
                maven { url PUBLIC_REPOSITORY }
                google()
                mavenCentral()
            }
        }
    }
    ````
2. 若需要wrapper下载加速可以替换你项目下gradle/wrapper/gradle-wrapper.properties文件内的下载地址
    ````properties
    # 此处为8.14版本，版本号不变
    distributionUrl=https\://services.gradle.org/distributions/gradle-8.14-bin.zip
    # 将其替换为（此处使用腾讯云镜像站）
    distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-8.14-bin.zip
    ````
   或使用代理：
    ````properties
    # 添加至gradle-wrapper.properties中
    systemProp.http.proxyHost=127.0.0.1
    systemProp.http.proxyPort=7890
    systemProp.https.proxyHost=127.0.0.1
    systemProp.https.proxyPort=7890
    ````

3. 如果github下载速度过慢可以去[国内镜像](https://wzpmc.cn:3000/wzp/CHS-Gradle/releases)下