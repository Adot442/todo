plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
    id("io.micronaut.application") version "4.6.2"
    id("com.gradleup.shadow") version "8.3.9"
    id("io.micronaut.aot") version "4.6.2"
    id("org.sonarqube") version "5.1.0.4882"
    id("jacoco")
}

version = "0.1"
group = "io.adot442.todo"

val kotlinVersion = project.properties.get("kotlinVersion")

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.openapi:micronaut-openapi")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    compileOnly("io.micronaut:micronaut-http-client")
    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut:micronaut-http-client")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "io.adot442.todo.ApplicationKt"
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

graalvmNative.toolchainDetection = false

// ─── JaCoCo ──────────────────────────────────────────────────────────────────
jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

// ─── SonarQube / SonarCloud ───────────────────────────────────────────────────
sonar {
    properties {
        property("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY") ?: "adot442_todo-server")
        property("sonar.organization", System.getenv("SONAR_ORGANIZATION") ?: "adot442")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "https://sonarcloud.io")
        property("sonar.sources", "src/main/kotlin")
        property("sonar.tests", "src/test/kotlin")
        property("sonar.java.binaries", "${layout.buildDirectory.get()}/classes/kotlin/main")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml"
        )
        property(
            "sonar.junit.reportPaths",
            "${layout.buildDirectory.get()}/test-results/test"
        )
        property("sonar.exclusions", "**/Application.kt")
    }
}

tasks.named("sonar") {
    dependsOn(tasks.jacocoTestReport)
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("io.adot442.todo.*")
    }
    aot {
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}
