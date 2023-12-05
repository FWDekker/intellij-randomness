import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.changelog.Changelog
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import java.time.Year

fun properties(key: String) = project.findProperty(key).toString()


/// Plugins
plugins {
    // Compilation
    id("org.jetbrains.kotlin.jvm") version "1.9.21"  // Use latest version, ignoring `gradle.properties`
    id("org.jetbrains.intellij") version "1.16.1"

    // Tests/coverage
    id("org.jetbrains.kotlinx.kover") version "0.7.5"

    // Static analysis
    id("io.gitlab.arturbosch.detekt") version "1.23.4"  // See also `gradle.properties`

    // Documentation
    id("org.jetbrains.changelog") version "2.2.0"
    id("org.jetbrains.dokka") version "1.9.10"  // See also `buildscript { dependencies` below and `gradle.properties`
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.10")  // See also `plugins` above and `gradle.properties`
        classpath("org.jetbrains.dokka:versioning-plugin:1.9.10")  // See also `plugins` above and `gradle.properties`
    }
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

    testImplementation("org.assertj:assertj-swing-junit:${properties("assertjSwingVersion")}")
    testRuntimeOnly("org.junit.platform:junit-platform-runner:${properties("junitRunnerVersion")}")
    testImplementation("org.junit.vintage:junit-vintage-engine:${properties("junitVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${properties("kotestVersion")}")
    testImplementation("io.kotest:kotest-framework-datatest:${properties("kotestVersion")}")
    testImplementation("io.kotest:kotest-runner-junit5:${properties("kotestVersion")}")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${properties("detektVersion")}")
    dokkaHtmlPlugin("org.jetbrains.dokka:versioning-plugin:${properties("dokkaVersion")}")
}


/// Configuration
tasks {
    // Compilation
    withType<JavaCompile> {
        sourceCompatibility = properties("javaVersion")
        targetCompatibility = properties("javaVersion")
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = properties("javaVersion")
            apiVersion = properties("kotlinApiVersion")
            languageVersion = properties("kotlinVersion")
        }
    }
    withType<Detekt> {
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
                if (changelog.has(properties("version"))) changelog.get(properties("version"))
                else changelog.getUnreleased(),
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
        if (project.hasProperty("kotest.tags")) systemProperty("kotest.tags", project.findProperty("kotest.tags")!!)

        useJUnitPlatform {
            if (!project.hasProperty("kotest.tags"))
                includeEngines("junit-vintage")

            includeEngines("kotest")
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
        notCompatibleWithConfigurationCache("cf. https://github.com/Kotlin/dokka/issues/1217")

        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            customAssets = listOf(file(".config/dokka/logo-icon.svg"))
            footerMessage = "&copy; ${Year.now().value} Florine&nbsp;W.&nbsp;Dekker"
        }
        pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
            if (project.hasProperty("dokka.pagesDir")) {
                val pagesDir = project.property("dokka.pagesDir")
                olderVersions = listOf(file("$pagesDir"))
                olderVersionsDir = file("$pagesDir/older/")
            }
        }
        moduleName.set("Randomness")
        moduleVersion.set("v${properties("version")}")
        offlineMode.set(true)
        suppressInheritedMembers.set(true)

        dokkaSourceSets {
            named("main") {
                includes.from(files("packages.md"))

                jdkVersion.set(properties("javaVersion").toInt())
                languageVersion.set(properties("kotlinVersion"))

                documentedVisibilities.set(
                    setOf(
                        Visibility.PUBLIC,
                        Visibility.PRIVATE,
                        Visibility.PROTECTED,
                        Visibility.INTERNAL,
                        Visibility.PACKAGE,
                    )
                )
                reportUndocumented.set(true)

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/FWDekker/intellij-randomness/tree/v${properties("version")}/src/main/kotlin"))
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}
