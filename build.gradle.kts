// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // 🔹 Agregar el plugin de Google Services para Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
    // 🔹 (Opcional, si usas Crashlytics o Performance Monitoring)
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}
