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
    id("org.jetbrains.kotlin.jvm") version "1.8.0"  // Set to latest version compatible with `pluginSinceBuild`, cf. https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
    id("org.jetbrains.intellij") version "1.17.3"

    // Tests/coverage
    id("org.jetbrains.kotlinx.kover") version "0.7.6"

    // Static analysis
    id("io.gitlab.arturbosch.detekt") version "1.23.6"  // cf. `gradle.properties`

    // Documentation
    id("org.jetbrains.changelog") version "2.2.0"
    id("org.jetbrains.dokka") version "1.9.20"  // cf. `buildscript { dependencies` below and `gradle.properties`
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka", "dokka-base", "1.9.20")  // cf. `plugins` above and `gradle.properties`
        classpath("org.jetbrains.dokka", "versioning-plugin", "1.9.20")  // cf. `plugins` above and `gradle.properties`
    }
}


/// Dependencies
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.uuid", "java-uuid-generator", properties("uuidGeneratorVersion")) {
        exclude(group = "org.slf4j", module = "slf4j-api")  // Logging API is already provided by IDEA platform
    }
    implementation("com.github.sisyphsu", "dateparser", properties("dateparserVersion")) {
        exclude(group = "org.projectlombok", module = "lombok")  // cf. https://github.com/sisyphsu/dateparser/issues/30
    }
    implementation("com.github.curious-odd-man", "rgxgen", properties("rgxgenVersion"))
    implementation("org.eclipse.mylyn.github", "org.eclipse.egit.github.core", properties("githubCore"))

    testImplementation("org.assertj", "assertj-swing-junit", properties("assertjSwingVersion"))
    testRuntimeOnly("org.junit.platform", "junit-platform-runner", properties("junitRunnerVersion"))
    testImplementation("org.junit.vintage", "junit-vintage-engine", properties("junitVersion"))
    testImplementation("io.kotest", "kotest-assertions-core", properties("kotestVersion"))
    testImplementation("io.kotest", "kotest-framework-datatest", properties("kotestVersion"))
    testImplementation("io.kotest", "kotest-runner-junit5", properties("kotestVersion"))

    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", properties("detektVersion"))
    dokkaHtmlPlugin("org.jetbrains.dokka", "versioning-plugin", properties("dokkaVersion"))
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
            apiVersion = properties("kotlinVersion")
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

    buildSearchableOptions {
        enabled = !project.hasProperty("build.hotswap")
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

// TODO[Remove workaround]: After https://github.com/Kotlin/dokka/issues/3398 has been fixed
abstract class DokkaHtmlPost : DefaultTask() {
    private val buildDir = project.layout.buildDirectory

    @TaskAction
    fun strip() {
        buildDir.dir("dokka/html/").get().asFile
            .walk()
            .filter { it.isDirectory && it.name.startsWith('.') }
            .forEach { it.deleteRecursively() }

        buildDir.dir("dokka/html/").get().asFileTree
            .forEach { file -> file.writeText(file.readText().dropLastWhile { it == '\n' } + '\n') }
    }
}
tasks.register<DokkaHtmlPost>("dokkaHtmlPost")
tasks.dokkaHtml { finalizedBy("dokkaHtmlPost") }
