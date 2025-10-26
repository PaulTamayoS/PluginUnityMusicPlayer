plugins {
    id("com.android.library")
    // id("org.jetbrains.kotlin.android") // Uncomment if using Kotlin
}

android {
    namespace = "com.Games4Science.PluginUnityMusicPlayer" // Required for AGP 7+ library modules
    compileSdk = 36

    defaultConfig    {
        minSdk = 23
    }

    buildTypes {
        getByName("release")
        {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug")
        {
            // Debug settings if needed
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        abortOnError = false // Optional: prevents build failure due to lint warnings
    }
}

dependencies {
    // Unity engine bridge (compile only — not included in final AAR)
    compileOnly(files("libs/classes.jar"))

    // Add this line to support ActivityResultLauncher & modern APIs
    //implementation("androidx.activity:activity:1.8.2")

    // Optional: if you ever need compatibility helpers for ActivityResultContracts
    // implementation "androidx.activity:activity-ktx:1.8.2"
}