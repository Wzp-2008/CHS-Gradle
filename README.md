# Gradle 国人优化计划（CHS-Gradle）

为国内开发者打造更顺畅的 Gradle 使用体验 —— 主要目标是让 Gradle 的源码包（src）从国内镜像下载（默认使用腾讯云镜像）。

---

## 主要功能

- ✅ 将 Gradle 的 src 源码包从任意国内镜像下载（默认：腾讯云镜像）
- ⚠️ snapshot 版本暂不支持（多数国内镜像没有 snapshot）
- ℹ️ 不影响 Gradle 的核心运行，仅修改源码包获取来源

---

## 快速开始（安装）

1. 下载 CHS-Gradle.jar，并放置到你的 Gradle 用户目录（通常为用户主目录下的 `.gradle` 文件夹）
2. 打开或创建：`~/.gradle/gradle.properties`
3. 在文件末尾添加（将路径替换为你的 CHS-Gradle.jar 的绝对路径）：
   ```text
   org.gradle.jvmargs=-javaagent:/full/path/to/CHS-Gradle.jar
   ```
4. 停止 Gradle daemon（使配置生效）：
   ```bash
   gradle --stop
   ```
   这条命令适用于绝大多数系统；如遇问题可重启终端或系统。

---

## 自定义源码下载源

- 默认使用：`https://mirrors.cloud.tencent.com/gradle`
- 若需切换为其他国内镜像，可在 `gradle.properties` 或环境变量中设置：
  ```text
  gradle.src.base=https://mirrors.cloud.tencent.com/gradle
  ```
  或在命令行/CI 环境中设置环境变量：
  ```bash
  export GRADLE_SRC_BASE=https://mirrors.cloud.tencent.com/gradle
  ```
  （支持以 `gradle.src.base` 或 `GRADLE_SRC_BASE` 为优先级的配置方式）

---

## 使用建议（让构建依赖/插件也使用国内镜像）

若你想把项目的依赖或 Gradle 插件都指向国内镜像，可把下面脚本放在 `settings.gradle` 或 `settings.gradle.kts`（Groovy 示例）：

```groovy
// 阿里云 gradle 插件镜像仓库地址
final PLUGIN_SOURCE = "https://maven.aliyun.com/repository/gradle-plugin"
// 阿里云 maven 镜像仓库地址
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
```

---

## Gradle Wrapper（distribution）加速建议

- 推荐方法：在项目内替换 `gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl`：
  ```properties
  # 示例（Gradle 8.14）
  distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-8.14-bin.zip
  ```

- 或者使用代理（在 `gradle-wrapper.properties` 中加入）：
  ```properties
  systemProp.http.proxyHost=127.0.0.1
  systemProp.http.proxyPort=7890
  systemProp.https.proxyHost=127.0.0.1
  systemProp.https.proxyPort=7890
  ```

注意：CHS-Gradle 无法对 wrapper 本体（Gradle binary 的下载）进行加速，替换 `distributionUrl` 或使用网络代理是可行方案。

---

## 限制与注意事项

- 不支持 snapshot 版本的 Gradle（因为多数国内镜像未同步 snapshot）
- CHS-Gradle 只是修改源码包下载来源，不改变构建逻辑或依赖解析规则
- 若你使用公司内网镜像或自建镜像，请确保镜像中包含目标 Gradle 版本的源码包

---

## 常见问题（FAQ）

Q: 如何确认 CHS-Gradle 生效？  
A: 在第一次需要下载 src 源码时，观察构建日志中下载源 URL；也可以临时开启 Gradle 的调试日志：`./gradlew assemble --debug` 并查看相关下载请求信息。

Q: 我想恢复默认行为，怎么做？  
A: 删除或注释 `gradle.properties` 中 `org.gradle.jvmargs=-javaagent:...` 这一行，然后执行 `gradle --stop`。

Q: 为什么有些版本没有源码包？  
A: 部分镜像没有同步全部版本或 snapshot，遇到缺失请尝试更换镜像或使用官方源（services.gradle.org）。

---

## 故障排查

- 若下载失败，请检查：
    - CHS-Gradle.jar 路径是否正确、文件是否损坏
    - 网络是否能访问配置的镜像地址
    - 镜像中是否存在对应版本的 `gradle-<version>-src.zip`

- 若遇到权限或 JVMAgent 加载错误，请确认：
    - 使用的 JDK 与 Gradle 兼容
    - 路径中无中文或空格造成的解析问题（建议使用无空格的绝对路径）

---

## 开发与贡献

欢迎提交 issue 或 PR 提出改进建议（例如：支持更多镜像源、增强日志、添加 GUI 设置等）。在提交 issue 前请尽量在 README 的“故障排查”部分自检。

---

## 许可（License）

本项目采用 MIT License（如需使用其他许可请在仓库中明确说明）。

---

感谢使用 CHS-Gradle！愿它能让你的 Gradle 开发体验更顺畅、在国内也能快速拿到源码调试与诊断所需的资源。