pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EasyMediaPicker"

// Legacy Android-only modules (unchanged)
include(":app")
include(":EasyMediaPicker")

// New KMP modules
include(":easy-media-picker-core")
include(":easy-media-picker-compose")




