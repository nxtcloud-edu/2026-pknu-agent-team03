// 순수 Java 단위 테스트용 설정 — Android SDK 없이 실행 가능
plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    main {
        java.srcDirs("../app/src/main/java")
    }
    test {
        java.srcDirs("../app/src/test/java")
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Android 스텁 (컴파일만 통과시키기 위한 최소한)
    compileOnly("androidx.annotation:annotation:1.8.0")
    compileOnly("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    compileOnly("androidx.lifecycle:lifecycle-livedata:2.8.3")
    compileOnly("androidx.fragment:fragment:1.8.1")
    compileOnly("javax.inject:javax.inject:1")
    compileOnly("com.google.dagger:hilt-android:2.51.1")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
