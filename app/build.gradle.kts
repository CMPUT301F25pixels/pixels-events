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
    implementation(libs.firebase.firestore)

    // JUnit 4 for unit testing
    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    
    // Mockito for mocking in unit tests
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    
    // Android testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-auth")
    implementation(libs.firebase.firestore) // If version catalog duplicates, keep – Gradle will unify.
    implementation("com.google.android.gms:play-services-tasks:18.1.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    // --- Imaging ---
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")

}
