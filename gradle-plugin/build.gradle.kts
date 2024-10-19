plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    alias(deps.plugins.lombok)
    // alias(deps.plugins.kotlin.jvm)
}

dependencies {
    // implementation(gradleKotlinDsl())
    compileOnly(deps.android.gradle)
    compileOnly(gradleApi())
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

gradlePlugin {
    plugins {
        create("golang") {
            id = "com.github.kr328.gradle.golang"
            implementationClass = "com.github.kr328.gradle.golang.GoProjectPlugin"
        }
    }
}