// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.10.0" apply false
    id("com.android.library")     version "8.10.0" apply false

    // Si usas el Kotlin Gradle Plugin en raíz, déjalo también o marca apply false:
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    // si también declaras el Kotlin JVM plugin:
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
}
