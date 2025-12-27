plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    
    pom {
        name.set("EasyMediaPicker Core")
        description.set("Core logic for EasyMediaPicker KMP library")
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
    
    // Web (JS) target
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
        
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
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
