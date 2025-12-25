plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    
    pom {
        name.set("EasyMediaPicker Compose")
        description.set("Compose Multiplatform UI for EasyMediaPicker")
        url.set("https://github.com/BasemNasr/EasyMediaPicker")
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        developers {
            developer {
                id.set("BasemNasr")
                name.set("Basem Nasr")
                email.set("basemnasr20@gmail.com")
            }
        }
        scm {
            url.set("https://github.com/BasemNasr/EasyMediaPicker")
            connection.set("scm:git:git://github.com/BasemNasr/EasyMediaPicker.git")
            developerConnection.set("scm:git:ssh://git@github.com/BasemNasr/EasyMediaPicker.git")
        }
    }
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
        publishLibraryVariants("release")
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
                implementation(project(":easy-media-picker-core"))
                implementation(compose.runtime)
                implementation(compose.foundation)
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
                implementation(compose.ui)
                implementation("androidx.activity:activity-compose:1.8.1")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
            }
        }
        
        // iosMain is automatically created by the default hierarchy template
        val iosMain by getting
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

android {
    namespace = "com.bn.easymediapicker.compose"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
}

