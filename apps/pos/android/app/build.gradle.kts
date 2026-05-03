plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

repositories {
    flatDir {
        dirs("libs/poslink")
    }
}

android {
    namespace = "com.posplatform.pos_app"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "com.posplatform.pos_app"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(mapOf("name" to "PAX_POSLinkAndroid_20250807", "ext" to "aar"))
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.jcraft:jsch:0.1.55")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.zxing:core:3.3.3")
    implementation("org.dom4j:dom4j:2.1.4")
}

flutter {
    source = "../.."
}
