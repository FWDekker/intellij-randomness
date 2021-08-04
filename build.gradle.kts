import java.net.URL
import java.time.Year

fun properties(key: String) = project.findProperty(key).toString()


/// Plugins
plugins {
    // Compilation
    id("org.jetbrains.kotlin.jvm") version "1.5.10"  // See also `gradle.properties`
    id("org.jetbrains.intellij") version "0.7.3"

    // Tests/coverage
    id("jacoco")

    // Static analysis
    id("io.gitlab.arturbosch.detekt") version "1.17.1"  // See also `gradle.properties`

    // Documentation
    id("org.jetbrains.dokka") version "1.4.32"
}


/// Dependencies
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.uuid:java-uuid-generator:${properties("uuidGeneratorVersion")}")
    implementation("com.vdurmont:emoji-java:${properties("emojiVersion")}")
    // Use bundled Kotlin (ca. 4MB) to ensure forwards compatibility with IDE versions
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${properties("mockitoKotlinVersion")}")
    testImplementation("org.assertj:assertj-core:${properties("assertjVersion")}")
    testImplementation("org.assertj:assertj-swing-junit:${properties("assertjSwingVersion")}")
    testImplementation("org.junit.platform:junit-platform-runner:${properties("junitRunnerVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties("junitVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties("junitVersion")}")
    testImplementation("org.junit.vintage:junit-vintage-engine:${properties("junitVersion")}")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:${properties("spekVersion")}")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${properties("spekVersion")}")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${properties("detektVersion")}")
}


/// Configuration
tasks {
    // Compilation
    withType<JavaCompile> {
        sourceCompatibility = properties("jvmVersion")
        targetCompatibility = properties("jvmVersion")
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = properties("jvmVersion")
            apiVersion = properties("kotlinApiVersion")
            languageVersion = properties("kotlinVersion")
        }
    }
    withType<io.gitlab.arturbosch.detekt.Detekt> {
        jvmTarget = properties("jvmVersion")
    }

    intellij {
        version = properties("intellijVersion")
        updateSinceUntilBuild = false
    }

    patchPluginXml {
        changeNotes(file("src/main/resources/META-INF/change-notes.html").readText())
        pluginDescription(file("src/main/resources/META-INF/description.html").readText())
        sinceBuild(properties("pluginSinceBuild"))
    }


    // Tests/coverage
    test {
        systemProperty("java.awt.headless", "false")
        systemProperty("spek2.execution.test.timeout", 0)

        useJUnitPlatform {
            includeEngines("junit-vintage", "junit-jupiter", "spek2")
        }

        testLogging {
            events = setOf(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED)
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    jacoco {
        toolVersion = properties("jacocoVersion")
    }

    jacocoTestReport {
        executionData(file("$buildDir/jacoco/test.exec"))

        sourceSets { sourceSets.main }

        reports {
            csv.isEnabled = false
            html.isEnabled = true
            xml.isEnabled = true
            xml.destination = file("$buildDir/reports/jacoco/report.xml")
        }

        dependsOn(test)
    }


    // Static analysis
    detekt {
        toolVersion = properties("detektVersion")
        allRules = true
        config = files(".config/detekt/.detekt.yml")
    }

    runPluginVerifier {
        ideVersions(properties("pluginVerifierIdeVersions"))
    }


    // Documentation
    dokkaHtml.configure {
        pluginsMapConfiguration.set(mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """{ "footerMessage": "© ${Year.now().value} F.W.&nbsp;Dekker" }"""
        ))
        moduleName.set("Randomness v${properties("version")}")
        offlineMode.set(true)

        dokkaSourceSets {
            named("main") {
                includes.from(files("packages.md"))

                jdkVersion.set(properties("javaVersion").toInt())

                includeNonPublic.set(false)
                skipDeprecated.set(false)
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/FWDekker/intellij-randomness/blob/master/src/main/kotlin"))
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}
