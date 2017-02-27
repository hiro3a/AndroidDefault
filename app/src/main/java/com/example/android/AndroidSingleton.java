package com.example.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Android用シングルトンクラス。
 *
 * @see <a href="http://www.atmarkit.co.jp/ait/articles/1611/25/news019.html">Javaでの常識が通用しないAndroidにおけるメモリ管理の注意点</a>
 */
public class AndroidSingleton {
    private final long mCreateTimestamp;

    private AndroidSingleton() {
        mCreateTimestamp = System.currentTimeMillis();
    }

    public static AndroidSingleton getInstance() {
        return MainApplication.getInstance().getAndroidSingleton();
    }

    @Override
    public String toString() {
        String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date(mCreateTimestamp));
        return getClass().getSimpleName() + ": " + time;
    }
}
