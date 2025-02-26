plugins {
    java
    id("application")
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.javaFx)
    alias(libs.plugins.jlink)
}

group = "org.nevertouchgrass"
version = "0.0.1"

application {
    mainClass.set("org.nevertouchgrass.prolific.ProlificApplication")
}

repositories {
    mavenCentral()
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

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation(libs.bundles.spring)
    implementation(libs.oshiCore)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junitJupiter)
    implementation(libs.log4j)
    implementation(libs.jacksonXml)
    implementation(libs.jacksonTypes)
    mockitoAgent(libs.mockito) { isTransitive = false }
}

tasks {
    test {
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
        jvmArgs("-Xshare:off")
        useJUnitPlatform()
    }

}
javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}
jlink {
    imageZip.set(file("${layout.buildDirectory}/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}

