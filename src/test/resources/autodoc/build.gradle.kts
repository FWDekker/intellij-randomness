plugins {
    id("java")
}

group = "com.fwdekker"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
