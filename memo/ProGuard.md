# ProGuard 設定

## Google Play Service
<pre>
-keep class com.google.android.gms.** { *; }
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
 
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
 
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
 
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
</pre>

## 参考
* [Androidで使用するProGuard勘案](http://outofmem.tumblr.com/post/91032541014/androidandroid%E3%81%A7%E4%BD%BF%E7%94%A8%E3%81%99%E3%82%8Bproguard%E5%8B%98%E6%A1%88)
* [Qiita:ProGuard設定まとめ](http://qiita.com/tsuyosh/items/9dd3c6b9dc11b5f640be)
