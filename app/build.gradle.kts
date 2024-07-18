plugins {
    `application`
    alias(libs.plugins.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":lib"))
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    implementation("org.apache.commons:commons-compress:1.26.2")
    implementation("com.github.luben:zstd-jni:1.5.6-3")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.calyxos.seedvault.chunker.MainKt")
}

tasks.register<Jar>("uberJar") {
    archiveBaseName = "seedvault-chunker"

    manifest {
        attributes["Main-Class"] = "org.calyxos.seedvault.chunker.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}