plugins {
    java
    id("application")
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

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.named("check") {
    dependsOn("spotlessApply")
}
