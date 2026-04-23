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

# AMap (高德地图) SDK ProGuard Rules
-keep class com.amap.api.** { *; }
-keep class com.autonavi.** { *; }
-dontwarn com.amap.api.**
-dontwarn com.autonavi.**
-keep class * implements com.amap.api.maps.AMap$OnCameraChangeListener { *; }
-keep class * implements com.amap.api.maps.AMap$OnMapLoadedListener { *; }
-keep class * implements com.amap.api.maps.AMap$OnMarkerClickListener { *; }
-keep class * implements com.amap.api.maps.AMap$OnInfoWindowClickListener { *; }
-keepattributes SourceFile,LineNumberTable
-keepclassmembers class * {
    @com.amap.api.maps.model.* <fields>;
    @com.amap.api.maps.model.* <methods>;
}
