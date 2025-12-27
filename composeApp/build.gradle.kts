plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    jvm("desktop")
    
    // Web (JS) target - Disabled for demo app due to Material3 incompatibility
    // The library itself supports web (see easy-media-picker-core and easy-media-picker-compose)
    // To create a web demo, use Compose for Web DOM APIs instead of Material3
    /*
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }
    */
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeDemo"
            isStatic = true
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                // implementation(compose.components.uiToolingPreview) // Unresolved in 1.5.11
                
                // EasyMediaPicker
                implementation(project(":easy-media-picker-core"))
                implementation(project(":easy-media-picker-compose"))

                // Coil for Image Loading
                implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha08")
                implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha08")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation("androidx.activity:activity-compose:1.8.2")
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        
        val iosMain by getting
        
        // jsMain disabled - see comment in targets section above
        /*
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }
        */
    }
}

android {
    namespace = "com.bn.easymediapicker.demo"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.bn.easymediapicker.demo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}
