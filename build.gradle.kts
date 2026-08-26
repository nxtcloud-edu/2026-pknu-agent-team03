plugins {
    id("com.android.application") version "8.11.1" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    id("org.springframework.boot") version "3.4.13" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    java
}

allprojects {
    group = "com.timeback"
    version = "0.1.0"
}

tasks.register("verifyAll") {
    group = "verification"
    description = "Runs device, domain, backup, integration, and Android UI unit tests."
    dependsOn(
        ":device-core:verifyData",
        ":domain:verifyDomain",
        ":backup:verifyBackup",
        ":backup:verifyConstructionIntegration",
        ":server:test",
        ":app:testDebugUnitTest"
    )
}
