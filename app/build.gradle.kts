plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.timeback.ui"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.timeback.ui"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.fragment:fragment:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.3")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("androidx.annotation:annotation:1.8.0")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    annotationProcessor("com.google.dagger:hilt-compiler:2.51.1")

    // Unit Test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
