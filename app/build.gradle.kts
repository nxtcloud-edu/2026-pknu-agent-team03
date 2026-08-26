import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.Sync

plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
}

val syncIntegratedMainSources by tasks.registering(Sync::class) {
    from("../src/main/java") {
        include("com/timeback/ui/**")
        include("com/timeback/device/os/**")
        include("com/timeback/device/room/**")
        include("com/timeback/backup/http/**")
    }
    into(layout.buildDirectory.dir("generated/integrated/main"))
}

val syncIntegratedTestSources by tasks.registering(Sync::class) {
    from("../src/test/java") {
        include("com/timeback/ui/**")
    }
    into(layout.buildDirectory.dir("generated/integrated/test"))
}

android {
    namespace = "com.timeback"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.timeback"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "TIMEBACK_BACKUP_BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "TIMEBACK_BACKUP_BASE_URL", "\"https://backup.invalid/\"")
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

    sourceSets {
        getByName("main") {
            java.srcDir(layout.buildDirectory.dir("generated/integrated/main"))
        }
        getByName("test") {
            java.srcDir(layout.buildDirectory.dir("generated/integrated/test"))
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.matching { task ->
    task.name == "preBuild" || task.name == "preDebugUnitTestBuild"
}.configureEach {
    if (name == "preBuild") {
        dependsOn(syncIntegratedMainSources)
    } else {
        dependsOn(syncIntegratedTestSources)
    }
}

dependencies {
    implementation(project(":device-core"))
    implementation(project(":domain"))
    implementation(project(":backup"))

    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment:1.8.5")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.navigation:navigation-fragment:2.8.5")
    implementation("androidx.navigation:navigation-ui:2.8.5")
    implementation("com.google.dagger:hilt-android:2.56.2")
    annotationProcessor("com.google.dagger:hilt-compiler:2.56.2")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.4")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    enableAssertions = true
}
