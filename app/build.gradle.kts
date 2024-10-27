plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.blockinger.game"
    compileSdk = 35

    defaultConfig {
        applicationId  = "com.machfour.blockinger"
        minSdk = 21
        targetSdk = 34
        versionCode = 14
        versionName = "1.8.2"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.cursoradapter)
    implementation(libs.androidx.constraintlayout)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.navigation.compose)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.activity.compose)
    implementation(libs.androidx.collection)

    // TODO was debugImplementation, figure out how to restore
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)


}
