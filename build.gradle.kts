import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.changelog.Changelog
import java.net.URL
import java.time.Year

fun properties(key: String) = project.findProperty(key).toString()


/// Plugins
plugins {
    // Compilation
    id("org.jetbrains.kotlin.jvm") version "1.8.22"  // See also `gradle.properties`
    id("org.jetbrains.intellij") version "1.14.2"

    // Tests/coverage
    id("org.jetbrains.kotlinx.kover") version "0.7.2"

    // Static analysis
    id("io.gitlab.arturbosch.detekt") version "1.23.0"  // See also `gradle.properties`

    // Documentation
    id("org.jetbrains.changelog") version "2.1.0"
    id("org.jetbrains.dokka") version "1.8.20"
}


/// Dependencies
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.uuid:java-uuid-generator:${properties("uuidGeneratorVersion")}")
    implementation("com.github.sisyphsu:dateparser:${properties("dateparserVersion")}")
    implementation("com.github.curious-odd-man:rgxgen:${properties("rgxgenVersion")}")
    implementation("com.vdurmont:emoji-java:${properties("emojiVersion")}")
    api("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.assertj:assertj-core:${properties("assertjVersion")}")
    testImplementation("org.assertj:assertj-swing-junit:${properties("assertjSwingVersion")}")
    testRuntimeOnly("org.junit.platform:junit-platform-runner:${properties("junitRunnerVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${properties("junitVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties("junitVersion")}")
    testImplementation("io.kotest:kotest-runner-junit5:${properties("kotestVersion")}")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${properties("detektVersion")}")
}


/// Configuration
tasks {
    // Compilation
    withType<JavaCompile> {
        sourceCompatibility = properties("javaVersion")
        targetCompatibility = properties("javaVersion")
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = properties("javaVersion")
            apiVersion = properties("kotlinApiVersion")
            languageVersion = properties("kotlinVersion")
        }
    }
    withType<io.gitlab.arturbosch.detekt.Detekt> {
        jvmTarget = properties("javaVersion")
    }

    intellij {
        version.set(properties("intellijVersion"))
        downloadSources.set(true)
        updateSinceUntilBuild.set(false)  // Set in `patchPluginXml`
    }

    patchPluginXml {
        changeNotes.set(provider {
            changelog.renderItem(
                changelog.getUnreleased(),
                Changelog.OutputType.HTML
            )
        })
        pluginDescription.set(file("src/main/resources/META-INF/description.html").readText())
        sinceBuild.set(properties("pluginSinceBuild"))
    }

    changelog {
        repositoryUrl.set("https://github.com/FWDekker/intellij-randomness")
        itemPrefix.set("*")
    }

    signPlugin {
        if (System.getenv("CERTIFICATE_CHAIN") != null) {
            certificateChainFile.set(file(System.getenv("CERTIFICATE_CHAIN")))
            privateKeyFile.set(file(System.getenv("PRIVATE_KEY")))
            password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
        }
    }


    // Tests/coverage
    test {
        systemProperty("java.awt.headless", "false")

        useJUnitPlatform {
            includeEngines("junit-vintage", "junit-jupiter", "kotest")
        }

        testLogging {
            events = setOf(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }

        finalizedBy(koverXmlReport)
    }

    koverReport {
        defaults {
            html { onCheck = false }
            xml { onCheck = false }
        }
    }


    // Static analysis
    detekt {
        allRules = true
        config.setFrom(".config/detekt/.detekt.yml")
    }

    runPluginVerifier {
        ideVersions.set(properties("pluginVerifierIdeVersions").split(","))
    }


    // Documentation
    dokkaHtml.configure {
        notCompatibleWithConfigurationCache("Not sure why")

        pluginsMapConfiguration.set(
            mapOf(
                "org.jetbrains.dokka.base.DokkaBase" to
                    """{ "footerMessage": "&copy; ${Year.now().value} Florine&nbsp;W.&nbsp;Dekker" }"""
            )
        )
        moduleName.set("Randomness v${properties("version")}")
        offlineMode.set(true)
        suppressInheritedMembers.set(true)

        dokkaSourceSets {
            named("main") {
                includes.from(files("packages.md"))

                jdkVersion.set(properties("javaVersion").toInt())

                includeNonPublic.set(true)
                skipDeprecated.set(false)
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/FWDekker/intellij-randomness/tree/v${properties("version")}/src/main/kotlin"))
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}
