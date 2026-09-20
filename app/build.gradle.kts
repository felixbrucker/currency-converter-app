plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.kover)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
  namespace = "com.felixbrucker.currencyconverter"
  compileSdk = 37

  defaultConfig {
    applicationId = "com.felixbrucker.currencyconverter"
    minSdk = 26
    targetSdk = 37
    versionCode = 7
    versionName = "1.1.1"
  }

  signingConfigs {
    getByName("debug") {
      val keystorePath = System.getenv("KEYSTORE_PATH")
      storeFile = if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
        file(keystorePath)
      } else {
        file("${rootDir}/debug.keystore")
      }
      storePassword = System.getenv("KEYSTORE_PASSWORD")
      keyAlias = System.getenv("KEY_ALIAS")
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH")
      storeFile = if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
        file(keystorePath)
      } else {
        file("${rootDir}/release.keystore")
      }
      storePassword = System.getenv("KEYSTORE_PASSWORD")
      keyAlias = System.getenv("KEY_ALIAS")
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
}
kover {
  reports {
    total {
      verify {
        rule {
          minBound(90)
        }
      }
    }
    filters {
      excludes {
        classes(
          "*.BuildConfig",
          "*_*",
          "*JsonAdapter*",
          "com.felixbrucker.currencyconverter.ui.composable.*",
          "com.felixbrucker.currencyconverter.ui.components.*",
          "com.felixbrucker.currencyconverter.ui.screens.*",
          "com.felixbrucker.currencyconverter.ui.theme.*",
          "com.felixbrucker.currencyconverter.ui.*",
          "com.felixbrucker.currencyconverter.MainActivity*",
        )
      }
    }
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.coil.compose)
  implementation(libs.coil.network.okhttp)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
