plugins {
    // Android
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    
    // Kotlin
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("org.jetbrains.kotlin.multiplatform") version "1.9.21" apply false
    
    // Compose Multiplatform
    id("org.jetbrains.compose") version "1.5.11" apply false
}

allprojects {
    group = "com.bn.easymediapicker"
    version = "2.0.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
