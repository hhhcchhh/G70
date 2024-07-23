# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

# 表示混淆时不使用大小写混合类名
-dontusemixedcaseclassnames
# 表示不跳过library中的非public的类
-dontskipnonpubliclibraryclasses
# 打印混淆的详细信息
-verbose
# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
#不进行优化，建议使用此选项，
-dontoptimize
# 表示不进行校验,这个校验作用 在java平台上的
-dontpreverify
#忽略警告
-ignorewarnings
#保证是独立的jar,没有任何项目引用,如果不写就会认为我们所有的代码是无用的,从而把所有的代码压缩掉,导出一个空的jar
-dontshrink
#保护泛型
-keepattributes Signature
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.
-keepattributes *Annotation*
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
# 比如，当内联一个公共的getter方法时，这也可能需要外地公共访问。
# 虽然java二进制规范不需要这个，要不然有的虚拟机处理这些代码会有问题。当有优化和使用-repackageclasses时才适用。
#指示语：不能用这个指令处理库中的代码，因为有的类和类成员没有设计成public ,而在api中可能变成public
-allowaccessmodification
# 保持哪些类不被混淆
#继承activity,application,service,broadcastReceiver,contentprovider....不进行混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.multidex.MultiDexApplication
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep class android.support.** {*;}## 保留support下的所有类及其内部类

-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
#表示不混淆上面声明的类，最后这两个类我们基本也用不上，是接入Google原生的一些服务时使用的。
# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**
# 这指定了继承Serizalizable的类的如下成员不被移除混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# 保留R下面的资源
#-keep class **.R$* {
# *;
#}
#不混淆资源类下static的
-keepclassmembers class **.R$* {
    public static <fields>;
}
# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    interface <methods>;
}
-keepclasseswithmembernames class * {
    SerialPort <methods>;
}
# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}
# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#表示不混淆Parcelable实现类中的CREATOR字段，
#毫无疑问，CREATOR字段是绝对不能改变的，包括大小写都不能变，不然整个Parcelable工作机制都会失败。
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
# ----------------------------- 第三方 -----------------------------
-dontwarn com.orhanobut.logger.**
-keep class com.orhanobut.logger.**{*;}
-keep interface com.orhanobut.logger.**{*;}
-dontwarn com.google.gson.**
-keep class com.google.gson.**{*;}
-keep interface com.google.gson.**{*;}

# -libraryjars libs/ganymed-ssh2-build210.jar
-dontwarn ch.ethz.ssh2.**
-keep class ch.ethz.ssh2.**{*;}
-keep interface ch.ethz.ssh2.**{*;}
# -libraryjars libs/Zip4j1.3.3.jar
-dontwarn net.lingala.zip4j.**
-keep class net.lingala.zip4j.**{*;}
-keep interface net.lingala.zip4j.**{*;}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
# Understand the @Keep support annotation.
-keep class android.support.annotation.Keep
-keep @android.support.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}
# 删除代码中Log相关的代码
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}
# =============================================================
# 以下为常用配置
# https://blog.csdn.net/chenliguan/article/details/103655204
# 不混淆某个类
# -keep public class name.huihui.example.Test { *; }
#  不混淆某个类的子类
# -keep public class * extends name.huihui.example.Test { *; }
#  不混淆所有类名中包含了“model”的类及其成员
# -keep public class **.*model*.** {*;}
#  不混淆某个接口的实现
# -keep class * implements name.huihui.example.TestInterface { *; }
#  不混淆某个类的构造方法
# -keepclassmembers class name.huihui.example.Test {
#     public <init>();
# }
#  不混淆某个类的特定的方法
# -keepclassmembers class name.huihui.example.Test {
#     public void test(java.lang.String);
# }
# 不混淆某个类的内部类
# -keep class name.huihui.example.Test$* {
#         *;
#  }
# 两个常用的混淆命令，注意：
# 一颗星表示只是保持该包下的类名，而子包下的类名还是会被混淆；
# 两颗星表示把本包和所含子包下的类名都保持；
# -keep class com.suchengkeji.android.ui.**
# -keep class com.suchengkeji.android.ui.*
# 用以上方法保持类后，你会发现类名虽然未混淆，但里面的具体方法和变量命名还是变了，
# 如果既想保持类名，又想保持里面的内容不被混淆，我们就需要以下方法了

# 不混淆某个包所有的类
# -keep class com.suchengkeji.android.bean.** { *; }
# =============================================================
# 不混淆某个类（使用者可以看到类名）
-keep class com.nr.NrSdk
-keepclassmembers class com.nr.NrSdk {
    public *;
}
-keep class com.nr.Util.Battery
-keepclassmembers class com.nr.Util.Battery {
    public *;
}
-keep class com.nr.Util.Temperature
-keepclassmembers class com.nr.Util.Temperature {
    public *;
}
-keep class com.nr.Util.GpsState
-keepclassmembers class com.nr.Util.GpsState {
    public *;
}
-keep class com.nr.Util.GpsOffset
-keepclassmembers class com.nr.Util.GpsOffset {
    public *;
}
-keep class com.nr.Util.AirState
-keepclassmembers class com.nr.Util.AirState {
    public *;
}
-keep class com.nr.Util.IpUtil
-keepclassmembers class com.nr.Util.IpUtil {
    public *;
}
-keep class com.nr.Util.GnbTimingOffset
-keepclassmembers class com.nr.Util.GnbTimingOffset {
    public *;
}
-keep class com.nr.Util.SdkPref
-keepclassmembers class com.nr.Util.SdkPref {
    public *;
}
-keep class com.nr.Util.OpLog
-keepclassmembers class com.nr.Util.OpLog {
    public *;
}
-keep class com.Logcat.SLog
-keepclassmembers class com.Logcat.SLog {
    public *;
}
-keep class com.Logcat.LogcatHandler
-keepclassmembers class com.Logcat.LogcatHandler {
    public *;
}
#-keep class com.nr.File.ZipPassll
#-keepclassmembers class com.nr.File.ZipPassll {
#    public *;
#}
# 不混淆某个包所有的类
-keep class com.nr.Bean.** { public *; }
-keep class com.nr.FTP.** { public *; }
-keep class com.nr.Gnb.** { public *; }
-keep class com.nr.Socket.** { public *; }
-keep class com.nr.Arfcn.Bean.** { public *; }
-keep class com.SerialPort.** { public *; }
# 不混淆某个类（使用者可以看到类名）
# -keep class com.nr.Arfcn.Bean.LocBean
# 不混淆内部类
-keepnames class com.nr.Arfcn.Nr5g$* {
     public <fields>;
     public <methods>;
}
# 不混淆某个类中以 public 开始的方法（使用者可以看到该方法）
-keepclassmembers class com.nr.Arfcn.Nr5g {
    public *;
}
-keepclassmembers class com.nr.Arfcn.ArfcnService {
    public *;
}

