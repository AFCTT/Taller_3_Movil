plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.taller3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.taller3"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core y Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.database)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)
    // Google Maps y Location
    implementation(libs.maps.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.maps.v1820)

    // Credential Manager (para Google Sign-In nuevo)
    implementation (libs.androidx.credentials)
    implementation (libs.androidx.credentials.v110)

    // Play Services Location y Maps (si los usas)
    implementation(libs.play.services.location.v2101)
    implementation (libs.play.services.maps.v1810)

    // Navegación Compose
    implementation(libs.androidx.navigation.compose)

// Accompanist para permisos
    implementation(libs.accompanist.permissions.v0340alpha)

    // Otros
    implementation(libs.androidx.appcompat)
    implementation(libs.googleid)

    implementation(libs.androidx.credentials.v130)
    implementation(libs.androidx.credentials.play.services.auth)

    implementation (libs.com.google.maps.android.maps.compose.v600)
    implementation (libs.play.services.maps)

    implementation (libs.maps.compose.v620)

    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")


    implementation (libs.ui) // Asegúrate de que la versión de Compose sea compatible
        implementation (libs.ui.graphics)
        implementation (libs.maps.compose.v260) // Versión compatible con Compose
        implementation (libs.play.services.maps)


    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
