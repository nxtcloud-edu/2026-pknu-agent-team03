plugins {
    java
    id("org.springframework.boot") version "3.4.13"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.timeback"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf("../../src/main/java", "../src/main/java"))
        java.include("com/timeback/backup/contracts/**")
        java.include("com/timeback/backup/server/**")
        java.include("com/timeback/server/**")
        resources.setSrcDirs(listOf("../src/main/resources"))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("com.h2database:h2")
}

tasks.bootJar {
    archiveFileName.set("timeback-server.jar")
}
