# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/linguancheng/Library/Android/sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-----------------不需要混淆第三方类库------------------------------------------------------------------

-ignorewarnings
-keepattributes Signature,*Annotation*

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment
-keep class com.goole.** { *; }
-keep class org.jdesktop.** { *; }
-keep class com.sun.** { *; }
-keep class org.jsoup.** { *; }

# keep okhttp3、okio
-dontwarn okhttp3.**
-keep class okhttp3.** { *;}
-keep interface okhttp3.** { *; }
-dontwarn okio.**

# keep rx
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#-----------------不需要混淆系统组件等-------------------------------------------------------------------

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

#-----------------不需要混淆JavaBean实体类-------------------------------------------------------------------
# 确保JavaBean不被混淆-否则gson将无法将数据解析成具体对象
-keep class edu.gdei.gdeiassistant.Pojo.**{*;}

#基线包使用，生成mapping.txt
-printmapping mapping.txt
#生成的mapping.txt在app/build/outputs/mapping/release路径下，移动到/app路径下

#修复后的项目使用，保证混淆结果一致
#-applymapping mapping.txt

#hotfix
-keep class com.taobao.sophix.**{*;}
-keep class com.ta.utdid2.device.**{*;}

#httpdns
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.ta.**{*;}
-keep class com.ut.**{*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.ta.**
-dontwarn com.ut.**

#防止inline
-dontoptimize

-keepclassmembers class edu.gdei.gdeiassistant.Application.GdeiAssistantApplication {
    public <init>();
}