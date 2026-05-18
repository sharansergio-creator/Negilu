plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.negilu.app"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.negilu.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPBOX_ACCESS_TOKEN"] = providers.gradleProperty("MAPBOX_ACCESS_TOKEN").orElse("").get()
        buildConfigField("String", "MAPBOX_TOKEN", "\"${providers.gradleProperty("MAPBOX_ACCESS_TOKEN").orElse("").get()}\"")
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"${providers.gradleProperty("OPENWEATHER_API_KEY").orElse("").get()}\"")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Mapbox
    implementation("com.mapbox.maps:android:11.9.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}