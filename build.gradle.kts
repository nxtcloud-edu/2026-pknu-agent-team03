plugins {
    id("com.android.application") version "8.11.1" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
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
        ":app:testDebugUnitTest"
    )
}
