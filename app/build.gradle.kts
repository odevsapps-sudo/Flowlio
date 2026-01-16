plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // ✅ Helyes plugin Room-hoz!
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.odevs.utilitymetertracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.odevs.utilitymetertracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    // Compose alapok
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.material:material-icons-extended")

    // PDF-hez (iText 7)
    implementation("com.itextpdf:itext7-core:8.0.2") {
        exclude(group = "org.slf4j") // hogy ne ütközzön más log könyvtárakkal
    }
    implementation("org.slf4j:slf4j-simple:1.7.36")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel + LiveData Compose támogatás
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")

    implementation("io.coil-kt:coil-compose:2.4.0")

    // Debug tooling
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")

    // ✅ Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Egyéb
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Teszt
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
