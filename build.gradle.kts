plugins {
    java
    alias(libs.plugins.jacoco)
    alias(libs.plugins.application)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.javaFx)
    alias(libs.plugins.beryx)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.generate.localization.provider)
}

group = "org.nevertouchgrass"
version = "1.0.1"

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
    implementation(libs.logging)
    implementation(libs.jacksonXml)
    implementation(libs.jacksonTypes)
    implementation(libs.sqlite)
    implementation(libs.hikari)
    implementation(libs.reactor)
    implementation(libs.richtext)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testImplementation(libs.bundles.testing)
    testImplementation(libs.mockito)
    testRuntimeOnly(libs.junitJupiter)
    mockitoAgent("org.mockito:mockito-core:5.14.0") { isTransitive = false }
}

tasks {
    test {
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
        jvmArgs("-Xshare:off")
        useJUnitPlatform()
        finalizedBy(jacocoTestCoverageVerification)
    }

    javadoc {
        setDestinationDir(file("${layout.projectDirectory}/docs"))
    }
}

javafx {
    version = "23.0.1"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

//jlink {
//    imageZip.set(file("${layout.buildDirectory}/distributions/app-${javafx.platform.classifier}.zip"))
//    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
//    launcher {
//        name = "Prolific"
//    }
//}

tasks.register<Exec>("runLinux") {
    description = "Run the application on Linux"
    group = "application"
    dependsOn(tasks.bootJar)
    workingDir = rootDir
    environment("GDK_BACKEND", "x11")
    commandLine("java", "-Dprism.order=sw", "-jar", "${tasks.bootJar.get().archiveFile.get().asFile}")
}

tasks.register<Exec>("runWindows") {
    description = "Run the application on Windows"
    group = "application"
    dependsOn(tasks.bootJar)
    workingDir = rootDir
    commandLine("java", "-Dprism.order=sw", "-jar", "${tasks.bootJar.get().archiveFile.get().asFile}")
}

tasks.register<Exec>("runMac") {
    description = "Run the application on Mac"
    group = "application"
    dependsOn(tasks.bootJar)
    workingDir = rootDir
    commandLine("java", "-jar", "${tasks.bootJar.get().archiveFile.get().asFile}")
}

runtime {
    modules.addAll(
        "java.base",
        "java.compiler",
        "java.datatransfer",
        "java.desktop",
        "java.instrument",
        "java.logging",
        "java.management",
        "java.management.rmi",
        "java.naming",
        "java.net.http",
        "java.prefs",
        "java.rmi",
        "java.scripting",
        "java.se",
        "java.security.jgss",
        "java.security.sasl",
        "java.smartcardio",
        "java.sql",
        "java.sql.rowset",
        "java.transaction.xa",
        "java.xml",
        "java.xml.crypto",
        "javafx.base",
        "javafx.controls",
        "javafx.fxml",
        "javafx.graphics",
        "javafx.media",
        "javafx.swing",
        "javafx.web",
        "jdk.accessibility",
        "jdk.attach",
        "jdk.charsets",
        "jdk.compiler",
        "jdk.crypto.cryptoki",
        "jdk.crypto.ec",
        "jdk.dynalink",
        "jdk.editpad",
        "jdk.graal.compiler",
        "jdk.graal.compiler.management",
        "jdk.hotspot.agent",
        "jdk.httpserver",
        "jdk.incubator.vector",
        "jdk.internal.ed",
        "jdk.internal.jvmstat",
        "jdk.internal.le",
        "jdk.internal.md",
        "jdk.internal.opt",
        "jdk.internal.vm.ci",
        "jdk.jartool",
        "jdk.javadoc",
        "jdk.jcmd",
        "jdk.jconsole",
        "jdk.jdeps",
        "jdk.jdi",
        "jdk.jdwp.agent",
        "jdk.jfr",
        "jdk.jlink",
        "jdk.jpackage",
        "jdk.jshell",
        "jdk.jsobject",
        "jdk.jstatd",
        "jdk.localedata",
        "jdk.management",
        "jdk.management.agent",
        "jdk.management.jfr",
        "jdk.naming.dns",
        "jdk.naming.rmi",
        "jdk.net",
        "jdk.nio.mapmode",
        "jdk.sctp",
        "jdk.security.auth",
        "jdk.security.jgss",
        "jdk.unsupported",
        "jdk.unsupported.desktop",
        "jdk.xml.dom",
        "jdk.zipfs"
    )

    distDir = layout.buildDirectory.dir("install/Prolific-boot").get().asFile
    jpackage {
        installerName = "ProlificInstaller"
        imageName = "Prolific"
        appVersion = "1.0.1"
        mainJar = "Prolific-1.0.1.jar"
        mainClass = "org.springframework.boot.loader.launch.JarLauncher"
        outputDir = "image"
        imageOptions = listOf(
            "--icon", "src/main/resources/icons/png/IconPl.icns",
        )
    }
}

tasks.jre {
//    setDependsOn(listOf<Task>())
//    dependsOn(tasks.startScripts)
}
tasks.jpackageImage {
//    setDependsOn(listOf<Task>())
//    dependsOn(tasks.jre)
    dependsOn(tasks.installBootDist)
    dependsOn(tasks.bootJar)
}

tasks.jpackage {
//    setDependsOn(listOf<Task>())
//    dependsOn(tasks.jre)
//    dependsOn(tasks.jpackageImage)
    dependsOn(tasks.installBootDist)
    dependsOn(tasks.bootJar)
}
tasks.jar {
    enabled = false
}
tasks.installDist {
    enabled = false
}

tasks.bootJar {
    dependsOn(tasks.test)
}