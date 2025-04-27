import org.jetbrains.kotlin.konan.properties.loadProperties

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

idea {
    module {
        isDownloadSources = true
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("dev.samstevens.totp:totp:1.7.1")
    testImplementation(kotlin("test"))
}

val localProperties = loadProperties(project.file("local.properties").absolutePath)

tasks.register<Copy>("copyPlugin") {
    dependsOn("jar")
    from(tasks.jar.get().archiveFile)
    into(localProperties.getProperty("pluginsPath"))
}

tasks.register<JavaExec>("startServer") {
    dependsOn("copyPlugin")
    workingDir(localProperties.getProperty("serverPath"))
    classpath(localProperties.getProperty("serverJar"))
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