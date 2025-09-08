import org.jetbrains.kotlin.gradle.targets.js.npm.importedPackageDir

plugins {
    application
    kotlin("jvm") version "2.2.10"
}

group = "valkey.kotlin_kotlin"

version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation("io.netty:netty-transport:4.2.5.Final")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("valkey.kotlin.MainKt")
}
