plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf("../src/main/java"))
        java.include("com/timeback/device/**")
        java.exclude("com/timeback/device/os/Android*.java")
    }
    test {
        java.setSrcDirs(listOf("../src/test/java"))
        java.include("com/timeback/device/**")
    }
}

tasks.test {
    enableAssertions = true
}

tasks.register<JavaExec>("verifyData") {
    group = "verification"
    description = "Runs the device-data contract regression runner."
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.timeback.device.DeviceDataTrackTest")
    jvmArgs("-ea")
}
