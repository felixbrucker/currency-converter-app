plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
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
    versionCode = 3
    versionName = "1.0.2"
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

androidComponents {
  onVariants(selector().withBuildType("release")) { variant ->
    variant.outputs.forEach { output ->
      output.outputFileName.set("currency-converter-${output.versionName.get()}.apk")
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
