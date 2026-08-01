# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep org.json classes and their members
-keep class org.json.** { *; }

# If you use any custom data classes for JSON mapping,
# consider adding @androidx.annotation.Keep to the classes
# or use the rule below to keep all classes in your model package:
# -keep class com.tkprof.hundredeightv.models.** { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep Application class to prevent instantiation errors
-keep class com.tkprof.hundredeightv.AppApplication { *; }