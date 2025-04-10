plugins {
    alias(libs.plugins.java.gradle.plugin)
}

repositories {
    mavenLocal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("generateLocalizationProviderInterface") {
            id = "org.nevertouchgrass.prolific.generate-localization-provider"
            implementationClass = "org.nevertouchgrass.prolific.gradle.plugins.GenerateLocalizationProviderInterfacePlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

