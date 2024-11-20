buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.secrets.gradle.plugin)
    }
}

// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}
