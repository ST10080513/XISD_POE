plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}



android {
    namespace = "com.example.xisd_poe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.xisd_poe"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    testImplementation ("org.robolectric:robolectric:4.10.3")
    testImplementation ("org.slf4j:slf4j-simple:2.0.9")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("androidx.activity:activity:1.9.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.google.android.material:material:1.6.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation ("com.google.android.material:material:1.3.0")
    implementation ("com.google.zxing:core:3.3.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    testImplementation ("io.mockk:mockk:1.13.5")           // Mocking library
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4") // Coroutine testing
    testImplementation ("junit:junit:4.13.2")               // JUnit for unit testing
    testImplementation ("androidx.test.ext:junit:1.1.5")   // Android JUnit extensions
    testImplementation ("androidx.test:core:1.5.0")      // Android test core library
    implementation ("net.bytebuddy:byte-buddy:1.14.0")// Check for the latest version compatible with Java 21
    testImplementation ("io.mockk:mockk:1.13.5") // Or latest
    testImplementation ("org.robolectric:robolectric:4.10.3")
}

