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

#-----------------系统组件等：保持可反射访问的核心组件-------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

#-----------------JavaBean 实体类（Gson 解析）-------------------------------------------------------------------
# 确保JavaBean不被混淆-否则gson将无法将数据解析成具体对象
-keep class cn.gdeiassistant.Pojo.** { *; }

#基线包使用，生成mapping.txt
-printmapping mapping.txt
#生成的mapping.txt在app/build/outputs/mapping/release路径下，移动到/app路径下

#修复后的项目使用，保证混淆结果一致
#-applymapping mapping.txt

-keepclassmembers class cn.gdeiassistant.application.GdeiAssistantApplication {
    public <init>();
}

#
# Retrofit 2 / OkHttp / Gson / Compose 现代化规则
#

# Retrofit 接口与注解
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# OkHttp / Okio 常见 warn 忽略（R8 已内置大部分规则）
-dontwarn okhttp3.**
-dontwarn okio.**

# 保留注解信息（Retrofit / Gson / Compose 等依赖）
-keepattributes *Annotation*