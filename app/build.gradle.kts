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
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("org.apache.commons:commons-compress:1.28.0")
    implementation("com.github.luben:zstd-jni:1.5.7-4")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("app.grapheneos.seedvault.chunker.MainKt")
}

tasks.register<Jar>("uberJar") {
    archiveBaseName = "seedvault-chunker"

    manifest {
        attributes["Main-Class"] = "app.grapheneos.seedvault.chunker.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
