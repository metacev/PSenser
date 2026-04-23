# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Room entities
-keep class com.tracemaster.data.local.entity.** { *; }

# Keep GPX/KML models
-keep class com.tracemaster.data.remote.model.** { *; }

# Hilt
-dontwarn dagger.internal.**
-keep class dagger.internal.** { *; }
-dontwarn javax.inject.**
-keep class javax.inject.** { *; }

# Google Play Services Location
-keep class com.google.android.gms.location.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
