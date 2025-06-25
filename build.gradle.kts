plugins {
    application
    kotlin("jvm") version "2.1.20"
}

group = "valkey.kotlin_kotlin"

version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("valkey.kotlin.kotlin.MainKt")
}
