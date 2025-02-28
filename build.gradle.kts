plugins {
    java
    alias(libs.plugins.application)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.javaFx)
    alias(libs.plugins.jlink)
    alias(libs.plugins.maven.publish)
}

group = "org.nevertouchgrass"
version = "0.0.1"

application {
    mainClass.set("org.nevertouchgrass.prolific.ProlificApplication")
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
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
    implementation(libs.logging)
    implementation(libs.jacksonXml)
    implementation(libs.jacksonTypes)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junitJupiter)
    testImplementation(libs.mockito)
    mockitoAgent("org.mockito:mockito-core:5.14.0") { isTransitive = false }
}

tasks {
    test {
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
        jvmArgs("-Xshare:off")
        useJUnitPlatform()
    }
}

javafx {
    version = "23.0.1"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

jlink {
    imageZip.set(file("${layout.buildDirectory}/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}

tasks.register<Exec>("runLinux") {
    dependsOn(tasks.bootJar)
    workingDir = rootDir
    environment("GDK_BACKEND", "x11")
    commandLine("java", "-Dprism.order=sw", "-jar", "build/libs/Prolific-0.0.1.jar")
}

tasks.register<Exec>("runWindows") {
    dependsOn(tasks.bootJar)
    workingDir = rootDir
    commandLine("java", "-Dprism.order=sw", "-jar", "build/libs/Prolific-0.0.1.jar")
}

tasks.register<Exec>("runMac") {
    dependsOn(tasks.bootJar)
    workingDir = rootDir
    commandLine("java", "-jar", "build/libs/Prolific-0.0.1.jar")
}