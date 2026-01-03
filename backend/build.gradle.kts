import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    java
}

group = "com.aigympro"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {

        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.flywaydb:flyway-core:10.17.3")
    implementation("org.flywaydb:flyway-database-postgresql:10.17.3")

    // DB & migrations
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")

    // Jackson (Java Time, etc.)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Swagger / OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
