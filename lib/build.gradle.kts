plugins {
    `java-library`
    alias(libs.plugins.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}
