plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pixel_events"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pixel_events"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- Core UI & Architecture ---
    implementation(libs.appcompat)
    implementation(libs.material) // Ensure version catalog pins >= 1.12.0 for Material3 theme
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // --- Firebase (Bill of Materials) ---
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-auth")
    implementation(libs.firebase.firestore) // If version catalog duplicates, keep – Gradle will unify.
    implementation("com.google.android.gms:play-services-tasks:18.1.0")

    // --- Imaging ---
    implementation("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")

    // --- QR / ZXing ---
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") // keep single occurrence
    implementation("com.google.zxing:core:3.5.3")

    // --- Unit Testing ---
    // Use a single JUnit 4 dependency (version catalog or explicit). Remove duplicate.
    testImplementation("junit:junit:4.13.2")
    // Mockito
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // --- Instrumented / UI Testing (Aligned AndroidX Test & Espresso versions) ---
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Even if libs.ext.junit present, explicit for alignment
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Fragment testing (debug only) - keep
    debugImplementation("androidx.fragment:fragment-testing:1.7.1")
}
