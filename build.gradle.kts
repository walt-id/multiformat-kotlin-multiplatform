// Copyright (c) 2022 Erwin Kok. BSD-3-Clause license. See LICENSE file for more details.
import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    val kotlinVersion = "1.9.22"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

//    `java-test-fixtures`
    signing
    `maven-publish`

//    alias(libs.plugins.build.kover)
//    alias(libs.plugins.build.ktlint)
//    alias(libs.plugins.build.nexus)
    alias(libs.plugins.build.versions)
    alias(libs.plugins.build.testlogger)
//    alias(libs.plugins.build.protobuf)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

group = "org.erwinkok.multiformat"
version = "1.2.0"

kotlin {
    jvm {
        withJava()
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
    js(IR) {
        nodejs {
            generateTypeScriptDefinitions()
        }
        binaries.library()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                implementation("io.ktor:ktor-network:2.3.7") // for Closeable

                implementation("io.github.oshai:kotlin-logging:5.1.0")
            }
        }
        val jvmMain by getting {
            dependencies {}
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                implementation(libs.kotlinx.coroutines.test)

                runtimeOnly(libs.junit.jupiter.engine)
            }
        }
        val jsMain by getting {
            dependencies {}
        }
        val jsTest by getting {
            dependencies {}
        }
    }

}

/*java {
    withSourcesJar()
    withJavadocJar()
}*/

/*dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.atomicfu)
    implementation(libs.kotlin.logging)
    implementation(libs.kotlin.serialization)

    implementation(libs.ipaddress)
    implementation(libs.ktor.network)
    *//*implementation(libs.result.monad)*//*
    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)

    testImplementation(testFixtures(libs.result.monad))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.coroutines.debug)

    testRuntimeOnly(libs.junit.jupiter.engine)
}*/

/*testlogger {
    theme = ThemeType.MOCHA
}*/

/*tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }

    withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }

    test {
        useJUnitPlatform()
    }
}*/

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

/*sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin", "build/generated/source/proto/main/java")
        }
    }
}*/

/*koverReport {
    filters {
        excludes {
            classes(
                "org.erwinkok.multiformat.multicodec.Codec",
                "org.erwinkok.multiformat.multicodec.GenerateKt*",
            )
        }
    }

    defaults {
        html {
            onCheck = true
        }

        verify {
            onCheck = true
            rule {
                isEnabled = true
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION
                bound {
                    minValue = 0
                    maxValue = 99
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}*/

/*
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Multiformats protocols")
                inceptionYear.set("2022")
                url.set("https://github.com/erwin-kok/multiformat")
                licenses {
                    license {
                        name.set("BSD-3-Clause")
                        url.set("https://opensource.org/licenses/BSD-3-Clause")
                    }
                }
                developers {
                    developer {
                        id.set("erwin-kok")
                        name.set("Erwin Kok")
                        email.set("erwin-kok@gmx.com")
                        url.set("https://github.com/erwin-kok/")
                        roles.set(listOf("owner", "developer"))
                    }
                }
                scm {
                    url.set("https://github.com/erwin-kok/multiformat")
                    connection.set("scm:git:https://github.com/erwin-kok/multiformat")
                    developerConnection.set("scm:git:ssh://git@github.com:erwin-kok/multiformat.git")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/erwin-kok/multiformat/issues")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
*/
