plugins {
    kotlin("jvm") version "2.1.10"
    idea
}

group = "moe.skjsjhb.mc.plugins"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
}

idea {
    module {
        isDownloadSources = true
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}