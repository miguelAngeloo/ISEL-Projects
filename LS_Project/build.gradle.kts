plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.1.10"
    application
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation(platform("org.http4k:http4k-bom:6.1.0.1"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-jetty")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("org.postgresql:postgresql:42.+")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
}

tasks.test {
    useJUnitPlatform()
    if (!project.hasProperty("jdbcTests")) {
        exclude("pt/isel/ls/data/jdbc/**")
    }
}

application {
    mainClass.set("pt.isel.ls.server.HTTPServerKt")
}

tasks.register<Copy>("copyRuntimeDependencies") {
    into("build/libs")
    from(configurations.runtimeClasspath)
}

tasks.named<Jar>("jar") {
    dependsOn("copyRuntimeDependencies")
    manifest {
        attributes["Main-Class"] = "pt.isel.ls.server.HTTPServerKt"
        attributes["Class-Path"] = "LS.jar " + configurations.runtimeClasspath.get().joinToString(" ") { it.name }
    }
}
