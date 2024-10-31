import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.changelog.Changelog
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI
import java.time.Year

fun properties(key: String) = project.findProperty(key).toString()


/// Plugins
plugins {
    // Compilation
    id("org.jetbrains.kotlin.jvm") version "1.9.21"  // Set to latest version compatible with `pluginSinceBuild`, see also https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
    id("org.jetbrains.intellij.platform") version "2.1.0"

    // Tests/coverage
    id("org.jetbrains.kotlinx.kover") version "0.8.3"

    // Static analysis
    id("io.gitlab.arturbosch.detekt") version "1.23.7"  // See also `gradle.properties`

    // Documentation
    id("org.jetbrains.changelog") version "2.2.1"
    id("org.jetbrains.dokka") version "1.9.20"  // See also `buildscript.dependencies` below and `gradle.properties`
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka", "dokka-base", "1.9.20")  // See also `plugins` above and `gradle.properties`
        classpath("org.jetbrains.dokka", "versioning-plugin", "1.9.20")  // See also `plugins` above and `gradle.properties`
    }
}


/// Dependencies
repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    implementation("com.fasterxml.uuid", "java-uuid-generator", properties("uuidGeneratorVersion")) {
        // Logging API is already provided by IDEA platform
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("com.github.sisyphsu", "dateparser", properties("dateparserVersion")) {
        // TODO[Workaround]: Remove after https://github.com/sisyphsu/dateparser/issues/30 has been fixed
        exclude(group = "org.projectlombok", module = "lombok")
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

    intellijPlatform {
        intellijIdeaCommunity(
            properties("intellijVersion"),
            useInstaller = !properties("intellijVersion").endsWith("EAP-SNAPSHOT")
        )

        pluginVerifier()
        zipSigner()
        instrumentationTools()

        testFramework(TestFrameworkType.Platform)
    }
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
            // Transforms "1.9.20" to "1.9"
            val kotlinApiVersion = properties("kotlinVersion").split(".").take(2).joinToString(".")

            jvmTarget = properties("javaVersion")
            apiVersion = kotlinApiVersion
            languageVersion = kotlinApiVersion
        }
    }
    withType<Detekt> {
        jvmTarget = properties("javaVersion")
    }

    intellijPlatform {
        buildSearchableOptions = !project.hasProperty("build.hotswap")

        pluginConfiguration {
            description = file("src/main/resources/META-INF/description.html").readText()
            changeNotes = provider {
                changelog.renderItem(
                    if (changelog.has(properties("version"))) changelog.get(properties("version"))
                    else changelog.getUnreleased(),
                    Changelog.OutputType.HTML
                )
            }

            ideaVersion {
                sinceBuild = properties("pluginSinceBuild")
                untilBuild = provider { null }
            }
        }

        signing {
            if (System.getenv("CERTIFICATE_CHAIN") != null) {
                certificateChainFile = file(System.getenv("CERTIFICATE_CHAIN"))
                privateKeyFile = file(System.getenv("PRIVATE_KEY"))
                password = System.getenv("PRIVATE_KEY_PASSWORD")
            }
        }

        pluginVerification {
            ides {
                properties("pluginVerifierIdeVersions").split(",").forEach { ide(it) }
            }
        }
    }

    changelog {
        repositoryUrl = "https://github.com/FWDekker/intellij-randomness"
        itemPrefix = "*"
        unreleasedTerm = "9.9.9-unreleased"
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

    kover {
        reports {
            total {
                html { onCheck = false }
                xml { onCheck = false }
            }
        }
    }


    // Static analysis
    detekt {
        allRules = true
        config.setFrom(".config/detekt/.detekt.yml")
    }


    // Documentation
    dokkaHtml.configure {
        notCompatibleWithConfigurationCache("See also https://github.com/Kotlin/dokka/issues/1217")

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
        moduleName = "Randomness"
        moduleVersion = "v${properties("version")}"
        offlineMode = true
        suppressInheritedMembers = true

        dokkaSourceSets {
            named("main") {
                includes.from(files("packages.md"))

                jdkVersion = properties("javaVersion").toInt()
                languageVersion = properties("kotlinVersion")

                documentedVisibilities =
                    setOf(
                        Visibility.PUBLIC,
                        Visibility.PRIVATE,
                        Visibility.PROTECTED,
                        Visibility.INTERNAL,
                        Visibility.PACKAGE,
                    )
                reportUndocumented = true

                sourceLink {
                    localDirectory = file("src/main/kotlin")
                    remoteUrl = URI("https://github.com/FWDekker/intellij-randomness/tree/v${properties("version")}/src/main/kotlin").toURL()
                    remoteLineSuffix = "#L"
                }
            }
        }
    }
}

// TODO[Workaround]: Remove after https://github.com/Kotlin/dokka/issues/3398 has been fixed
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
