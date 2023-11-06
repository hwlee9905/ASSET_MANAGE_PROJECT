# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/donghunlee/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-dontwarn org.apache.**

#about library duplicate note
-dontnote org.apache.http.**
-dontnote org.apache.commons.**
-dontnote android.net.http.**

#about entry point descriptor classes
-dontnote com.mcnc.bizmob.core.view.slidingmenu.**
-dontnote com.mcnc.bizmob.core.view.webview.**

-keepattributes Exceptions,*Annotation*,InnerClasses,SourceFile,LineNumberTable,Deprecated,Signature,EnclosingMethod

#-verbose

#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
-keep public class * extends android.os.AsyncTask


-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

-keep class org.apache.** { *; }

#About Zxing library
-keep class com.google.zxing.**{*;}
-keep class com.journeyapps.**{*;}
-keep class com.journeyapps.barcodescanner.**{*;}

#-keep class com.mcnc.bizmob.core.manager.BMCManager {*;}
