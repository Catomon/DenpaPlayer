import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.junit)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            implementation("com.darkrockstudios:mpfilepicker:3.1.0")
            implementation("com.github.Vatuu:discord-rpc:1.6.2")
            implementation("dev.arbjerg:lavaplayer:2.1.2")
            implementation("com.github.goxr3plus:java-stream-player:9.0.4")

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        desktopMain.dependencies {
            implementation("org.slf4j:slf4j-api:2.0.7")
            implementation("org.slf4j:slf4j-simple:2.0.7")
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "com.github.catomon.denpaplayer"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.github.catomon.denpaplayer"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 6
        versionName = "1.1.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        implementation("androidx.media3:media3-exoplayer:1.3.0")
        implementation("androidx.media3:media3-exoplayer-dash:1.3.0")
        implementation("androidx.media3:media3-ui:1.3.0")

        debugImplementation(libs.compose.ui.tooling)
    }
    buildFeatures {
        viewBinding = true
    }
}
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi) //TargetFormat.Dmg,, TargetFormat.Deb
            packageName = "DenpaPlayer"
            packageVersion = "1.1.6"

            modules("java.compiler", "java.instrument", "java.naming", "java.scripting", "java.security.jgss", "java.sql", "jdk.management", "jdk.unsupported")

            buildTypes.release.proguard {
                configurationFiles.from(project.file("compose-desktop.pro"))
                isEnabled = false
            }

            windows {
                iconFile.set(project.file("denpa.ico"))
                shortcut = true
            }
        }
    }
}