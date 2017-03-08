# Gradle

## 署名
[アプリに署名する](https://developer.android.com/studio/publish/app-signing.html?hl=ja)

## Javadoc
[AndroidStudioでのJavaDocの記述と生成](http://qiita.com/k-yamada@github/items/fa77864b31919c0b765a)
<pre>
project.ext {
    if (android.hasProperty('applicationVariants')) {
        androidVariants = android.applicationVariants
    }else if (android.hasProperty('libraryVariants')) {
        androidVariants = android.libraryVariants
    }
}
project.androidVariants.all { variant ->
    task("javadoc", type: Javadoc, overwrite: true) {
        title = "YOUR_APP_NAME"
        source = variant.javaCompile.source
        ext.androidJar = "${android.sdkDirectory}/platforms/${android.compileSdkVersion}/android.jar"
        classpath = files(variant.javaCompile.classpath.files) + files(ext.androidJar)

        options {
            links("http://docs.oracle.com/javase/jp/7/api/");
            linksOffline("http://d.android.com/reference", "${android.sdkDirectory}/docs/reference")
            setMemberLevel(JavadocMemberLevel.PUBLIC)
            docEncoding = 'UTF-8'
            encoding = 'UTF-8'
            charSet = 'UTF-8'
        }

        exclude '**/BuildConfig.java'
        exclude '**/R.java'
    }
}
</pre>

## 参考
* [ビルドの設定](https://developer.android.com/studio/build/index.html?hl=ja)
* [Gradle Tips and Recipes](https://developer.android.com/studio/build/gradle-tips.html)
* [僕の考えた最強で平凡なbuild.gradle](https://wasabeef.jp/android-gradle/)
* [有名なAndroid オープンソースアプリ2つのbuild.gradleを読む](http://qiita.com/takahirom/items/9919697580fa3919df88)