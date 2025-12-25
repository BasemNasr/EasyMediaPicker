// Top-level build file for EasyMediaPicker KMP library
plugins {
    kotlin("multiplatform") version "1.9.20" apply false
    kotlin("android") version "1.9.20" apply false
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.compose") version "1.5.11" apply false
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
}

// Shared version info for all modules
val libraryVersion = "2.1.0"
val libraryGroup = "io.github.basemnasr-labs"

// Load local.properties
val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

localProperties.forEach { key, value ->
    project.extensions.extraProperties[key as String] = value
}

allprojects {
    group = libraryGroup
    version = libraryVersion
}

// No subprojects block needed for publishing config - handled by plugin in modules or here if using allprojects (preferred plugin application in submodules).
