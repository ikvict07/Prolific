plugins {
    java
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
    id("com.diffplug.spotless") version "6.22.0"
}

spotless {
    java {
        eclipse()
    }
}
repositories {
    mavenCentral()
}
javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
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