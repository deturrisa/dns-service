import com.diffplug.gradle.spotless.JavaExtension

plugins {
    java
    id("org.springframework.boot") version "3.2.11"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.20.0"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    //Spring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    //Open API
    implementation("com.google.code.findbugs:jsr305:3.0.1")
    implementation("org.openapitools:jackson-databind-nullable:0.2.1")

    //Serialize
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.postgresql:postgresql:42.3.9")

    implementation("commons-validator:commons-validator:1.6")
    implementation("org.mozilla:rhino:1.7.12")

    // AWS
    implementation("software.amazon.awssdk:sts:2.20.110")
    implementation("software.amazon.awssdk:route53:2.20.110")

    // Kafka
    implementation("org.apache.kafka:kafka-clients")
    implementation("org.apache.kafka:connect-json")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.bouncycastle:bcprov-jdk18on:1.78")

    implementation("com.nimbusds:nimbus-jose-jwt:9.37.2")

    implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.25")

    implementation("org.apache.avro:avro:1.11.4")
    implementation("org.apache.commons:commons-lang3:3.17.0")

    //Tests

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    //Required for MVC style testing.

    implementation ("org.springframework:spring-webmvc:6.1.14")
    implementation ("org.springframework:spring-web:6.1.14")

    testImplementation("io.zonky.test:embedded-database-spring-test:2.5.0")
    testImplementation("io.zonky.test:embedded-postgres:2.0.6")

    testImplementation("org.apache.velocity:velocity-engine-core:2.3")
    // JUnit 5 support
    testImplementation("org.junit.jupiter:junit-jupiter")

    //embedded kafka
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.awaitility:awaitility")

    //test containers
    testImplementation("org.testcontainers:postgresql:1.18.3")
    testImplementation("org.testcontainers:kafka:1.18.3")
    testImplementation("org.testcontainers:mockserver:1.18.3")
    testImplementation("org.testcontainers:junit-jupiter:1.18.3")
    testImplementation("org.mock-server:mockserver-client-java:5.15.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat("1.17.0")
        target("src/**/*.java")
        removeUnusedImports()
        validateImports()
        enforce120CharLimit()
    }
}

tasks.named("check") {
    dependsOn("spotlessCheck") // Ensures `spotlessCheck` runs as part of your checks
}

fun JavaExtension.validateImports() {
    custom("noWildcardImports") { str ->
        if (str.contains(".*;")) {
            throw IllegalArgumentException("Wildcard imports are not allowed.")
        }
        str
    }
}

fun JavaExtension.enforce120CharLimit() {
    custom("maxLineLength") { content ->
        content.lines().forEach { line ->
            if (line.length > 120) {
                throw IllegalArgumentException("Line exceeds 120 characters: $line")
            }
        }
        content
    }
}