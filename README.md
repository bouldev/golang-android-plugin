# Golang Android Plugin

Golang building plugin for Android project.

### Usage

- Add to buildscript 
  ```kotlin
  repositories {
      // ...
      maven("https://raw.githubusercontent.com/bouldev/maven-repo/main/releases")
  }
  dependencies {
      // ...
      classpath("com.github.kr328.golang:gradle-plugin:1.0.6")
  }
  ```

- Apply plugin to project
  ```kotlin
  plugins {
      id("com.github.kr328.gradle.golang")
  }
  ```

- Configure your source and build flags
  ```kotlin
  android {
      golang {
          libraryName = "my-golang-library"
          packageName = "my-golang/application"
          moduleDirectory = "src/main/go"
      }
      // buildFlags must add to productFlavors or buildType
      productFlavors {
          create("dev") {
              extensions.configure<GoVariantExtension> {
                  buildTags.addAll(setOf("enable_dev_features"))
              }
          }
          all {
              extensions.configure<GoVariantExtension> {
                  buildTags.addAll(setOf("android"))
              }
          }
      }
      buildType {
          debug {
              extensions.configure<GoVariantExtension> {
                  buildTags.addAll(setOf("enable_debug_trace"))
              }
          }
      }
  }
  ```