plugins {
    java
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.22.0"
}

group = "org.nevertouchgrass"
version = "0.0.1"

application {
    mainClass.set("org.nevertouchgrass.prolific.ProlificApplication")
}

spotless {
    java {
        eclipse()
    }
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    mavenLocal()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(file("${layout.buildDirectory}/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}

tasks.named("check") {
    dependsOn("spotlessApply")
}