# Phone Monitor

采集并显示手机基础硬件数据（电池温度、电量、电压、电流、健康状态等）。

## 开发方式

**本机只写代码，编译打包全部在 GitHub Actions 云端完成。**

本机不需要 Android Studio / Android SDK / Gradle。

```
写代码（VSCode） → git push → GitHub Actions 构建 → 下载 apk → 装到真机
```

## 目录分层

| 路径 | 层 | 说明 |
|---|---|---|
| `.github/workflows/` | 环境层 | 通用流水线，可整块抽走复用到其他项目 |
| `gradle/`、`*.gradle.kts`、`gradle.properties` | 环境层 | 构建配置与优化约定 |
| `app/` | 应用层 | 换应用只需替换此目录，流水线不动 |

## 环境要求

| 组件 | 版本 |
|---|---|
| JDK（云端） | 17 |
| Gradle | 8.9 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |

## 构建

```bash
./gradlew assembleDebug
```

云端由 `.github/workflows/android-build.yml` 自动执行，产物在 Actions 页面的 Artifacts 区域。

## 安装到真机

```bash
adb install -r app-debug.apk
adb logcat | grep phonemonitor    # 查看日志
```

## 构建优化约定

详见上级目录 `../Android环境评估与方案.md` 第八节。摘要：

1. `gradle/actions/setup-gradle@v4` 自动缓存
2. `paths` 过滤，改文档不触发构建
3. `concurrency.cancel-in-progress` 避免排队
4. `assembleDebug` 而非 `build`
5. debug 禁用 R8 / 资源缩减
6. **依赖最小化**——新增依赖前先问「必须吗」
7. `resourceConfigurations` 限制语言资源
8. 锁定 AGP / Gradle / JDK 版本
9. 每周 `cache-warmup.yml` 保持缓存热度

目标：冷构建 8–15 分钟，缓存命中后 2–4 分钟。

## 已知限制

**CPU 具体温度（摄氏度）在 Android 10+ 上第三方应用无法读取。**
`/sys/class/thermal/` 受 SELinux 策略限制；`HardwarePropertiesManager` 需要系统签名权限。
电池温度是唯一能稳定拿到具体度数的温度指标。
