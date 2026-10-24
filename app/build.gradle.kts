// ===== 应用层 =====
// 约定：不得在此文件写入流水线 / 签名 / 环境相关逻辑，
//      保证 app/ 模块可被整体替换而流水线无需改动。

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.fred.phonemonitor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fred.phonemonitor"
        minSdk = 26          // Android 8，覆盖荣耀 X50i+ 与华为 P30 兼容层
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // 优化点 #7：限制语言资源，减少资源合并开销
        resourceConfigurations.add("zh")
        resourceConfigurations.add("en")
    }

    buildTypes {
        getByName("debug") {
            // 优化点 #5：debug 关闭 R8 与资源缩减（构建中最耗时的一步）
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // 优化点 #6：依赖最小化。新增依赖前先问「这是必须的吗？」
    // 每增加一个库 = 编译时间 + dex 时间增加。
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
