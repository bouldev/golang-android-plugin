@file:Suppress("UnstableApiUsage")

rootProject.name = "golang-android-plugin"

include("gradle-plugin")

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            val agp = "8.7.0"
            val lombok = "6.4.1"
            //val kotlin = "2.0.21"

            library("android-gradle", "com.android.tools.build:gradle:$agp")
            //plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version(kotlin)
            //library("android-application", "com.android.application:gradle:$agp")
            plugin("lombok", "io.freefair.lombok").version(lombok)
        }
    }
}
