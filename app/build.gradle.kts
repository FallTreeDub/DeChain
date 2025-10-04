import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "jp.kozu_osaka.android.kozuzen"
    compileSdk = 35

    defaultConfig {
        applicationId = "jp.kozu_osaka.android.kozuzen"
        minSdk = 30
        targetSdk = 36
        compileSdk = 36
        versionCode = 1
        versionName = "Early_Test 1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if(localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { stream ->
                localProperties.load(stream)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }
    packaging {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.google.api.client)
    implementation(libs.google.api.services.sheets)
    implementation(libs.google.auth)
    implementation(libs.google.oauth)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.auto.service)
    implementation(libs.gson)
    implementation(libs.worker)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}