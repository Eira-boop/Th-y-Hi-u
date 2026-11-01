plugins {
    id("com.android.application")
}

android {
    namespace = "vn.edu.mku.foodsuggest"
    compileSdk = 34

    defaultConfig {
        applicationId = "vn.edu.mku.foodsuggest"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

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

    androidResources {
        noCompress += listOf("tflite")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // CameraX (Chương 1)
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // OpenCV (Chương 2)
    implementation("org.opencv:opencv:4.9.0")

    // Room Database (Chương 3)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Image loading (Chương 3)
    implementation("com.squareup.picasso:picasso:2.8")

    // TensorFlow Lite (Chương 4 & Chương 5)
    // Model mới cần FULLY_CONNECTED v12 -> TFLite 2.14/2.16 quá cũ, dùng LiteRT (tên mới của TFLite).
    // LiteRT vẫn giữ nguyên package org.tensorflow.lite nên không phải sửa code Java.
    implementation("com.google.ai.edge.litert:litert:1.0.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// Loại bỏ TFLite cũ do tensorflow-lite-support / task-vision kéo theo,
// để chỉ còn LiteRT (tránh trùng lớp và tránh dùng nhầm runtime cũ).
configurations.all {
    exclude(group = "org.tensorflow", module = "tensorflow-lite")
    exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
}