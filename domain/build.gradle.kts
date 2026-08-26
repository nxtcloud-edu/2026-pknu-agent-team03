plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    api(project(":device-core"))
}

sourceSets {
    main {
        java.setSrcDirs(listOf("../src/main/java"))
        java.include("io/timeback/domain/**")
    }
    test {
        java.setSrcDirs(listOf("../src/test/java"))
        java.include("io/timeback/domain/**")
    }
}

tasks.test {
    enableAssertions = true
}

tasks.register<JavaExec>("verifyDomain") {
    group = "verification"
    description = "Runs the pure-domain regression runner."
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.timeback.domain.DomainEngineTestRunner")
    jvmArgs("-ea")
}
