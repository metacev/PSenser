plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.sensormonitor"
    compileSdk = property("compileSdkVersion").toString().toInt()

    defaultConfig {
        applicationId = "com.example.sensormonitor"
        minSdk = property("minSdkVersion").toString().toInt()
        targetSdk = property("targetSdkVersion").toString().toInt()
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
        sourceCompatibility = JavaVersion.valueOf(property("javaVersion").toString())
        targetCompatibility = JavaVersion.valueOf(property("javaVersion").toString())
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:${property("appcompatVersion")}")
    implementation("com.google.android.material:material:${property("materialVersion")}")
    implementation("androidx.constraintlayout:constraintlayout:${property("constraintlayoutVersion")}")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${property("lifecycleVersion")}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${property("lifecycleVersion")}")
    implementation("androidx.activity:activity-ktx:${property("activityKtxVersion")}")
    implementation("androidx.fragment:fragment-ktx:${property("fragmentKtxVersion")}")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${property("coroutinesVersion")}")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:${property("navigationVersion")}")
    implementation("androidx.navigation:navigation-ui-ktx:${property("navigationVersion")}")
    
    // Testing
    testImplementation("junit:junit:${property("junitVersion")}")
    androidTestImplementation("androidx.test.ext:junit:${property("androidxJunitVersion")}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${property("espressoCoreVersion")}")
}
