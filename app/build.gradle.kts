plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    // Removed the "org.jetbrains.kotlin.android" line that was causing the crash
}

android {
    namespace = "com.example.canteenautomation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.canteenautomation"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")

    // UI & Design (Updated for your My Profile screen)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.activity:activity:1.9.0")

    // Circular Image View for the Avatar
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // --- FROM FRIEND: Google Play Services for Auth & OTP ---
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.1")

    // --- FROM FRIEND: Razorpay Payment Gateway ---
    implementation("com.razorpay:checkout:1.6.38")

    implementation("com.github.bumptech.glide:glide:4.16.0")
}