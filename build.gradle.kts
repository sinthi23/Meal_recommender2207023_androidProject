buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.2")  // Updated
        classpath("com.google.gms:google-services:4.4.2")
    }
}

plugins {
    id("com.android.application") version "8.3.2" apply false  // Updated
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}