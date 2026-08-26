plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":device-core"))
    testImplementation(project(":domain"))
}

sourceSets {
    main {
        java.setSrcDirs(listOf("../src/main/java"))
        java.include("com/timeback/backup/**")
    }
    test {
        java.setSrcDirs(listOf("../src/test/java"))
        java.include("com/timeback/backup/**")
        java.include("com/timeback/integration/**")
    }
}

tasks.test {
    enableAssertions = true
}

val verifyBackupClient by tasks.registering(JavaExec::class) {
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.timeback.backup.BackupClientTest")
    jvmArgs("-ea")
}

val verifyBackupServer by tasks.registering(JavaExec::class) {
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.timeback.backup.BackupServerTest")
    jvmArgs("-ea")
}

val verifyDataControl by tasks.registering(JavaExec::class) {
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.timeback.backup.DataControlClientTest")
    jvmArgs("-ea")
}

tasks.register("verifyBackup") {
    group = "verification"
    description = "Runs all backup and deletion regression runners."
    dependsOn(verifyBackupClient, verifyBackupServer, verifyDataControl)
}

tasks.register<JavaExec>("verifyConstructionIntegration") {
    group = "verification"
    description = "Runs the CT-01~CT-06 four-track integration smoke test."
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.timeback.integration.ConstructionTrackIntegrationTest")
    jvmArgs("-ea")
}
