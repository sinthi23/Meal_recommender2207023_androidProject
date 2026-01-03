import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}
val googleApiKey: String =
    gradleLocalProperties(
        rootDir,
        providers = TODO()
    ).getProperty("GOOGLE_API_KEY")
        ?: ""


android {
    namespace = "com.example.mealrecmmenderandroid"
    compileSdk = 35  // Updated from 34 to 35

    defaultConfig {
        applicationId = "com.example.mealrecmmenderandroid"
        minSdk = 24
        targetSdk = 34  // Keep at 34 for now
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    android {
        namespace = "com.example.mealrecmmenderandroid"

        defaultConfig {
            applicationId = "com.example.mealrecmmenderandroid"

            buildConfigField(
                "String",
                "GOOGLE_API_KEY",
                "\"$googleApiKey\""
            )
        }

        buildFeatures {
            viewBinding = true
            buildConfig = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")  // Updated
    implementation("com.google.android.material:material:1.12.0")  // Updated
    implementation("androidx.activity:activity:1.9.0")  // Updated
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")

    // RecyclerView and CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Image Loading - Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Credentials - Updated to compatible versions or removed
    implementation("androidx.credentials:credentials:1.3.0")  // Downgraded for compatibility
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")  // Downgraded
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")  // Updated
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")  // Updated
}