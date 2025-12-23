plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    // Apply the default hierarchy template for automatic source set setup
    applyDefaultHierarchyTemplate()
    
    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // Desktop (JVM) target
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("androidx.activity:activity-ktx:1.8.1")
                implementation("androidx.fragment:fragment-ktx:1.6.2")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            }
        }
        
        // iosMain is automatically created by the default hierarchy template
        val iosMain by getting
        
        val desktopMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
            }
        }
    }
}

android {
    namespace = "com.bn.easymediapicker.core"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
