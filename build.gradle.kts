// Top-level build file for EasyMediaPicker KMP library
plugins {
    kotlin("multiplatform") version "1.9.20" apply false
    kotlin("android") version "1.9.20" apply false
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.compose") version "1.5.11" apply false
    id("com.vanniktech.maven.publish") version "0.30.0" apply false
}

// Shared version info for all modules
val libraryVersion = "2.2.0"
val libraryGroup = "io.github.basemnasr-labs"

// Load local.properties and propagate to all projects
val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

allprojects {
    group = libraryGroup
    version = libraryVersion
    
    // Explicitly set properties from local.properties if they exist
    localProperties.forEach { key, value ->
        project.extensions.extraProperties[key as String] = value
    }
}

// No subprojects block needed for publishing config - handled by plugin in modules or here if using allprojects (preferred plugin application in submodules).
