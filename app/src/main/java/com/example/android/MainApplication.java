package com.example.android;

import android.app.Application;
import android.os.StrictMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import timber.log.Timber;

/**
 * アプリケーション。
 */
public class MainApplication extends Application {

    private static MainApplication sInstance;
    private AndroidSingleton mAndroidSingleton;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // StrictMode 対応
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .penaltyDeath()
                            .build());
            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .penaltyDeath()
                            .build());
        }

        // Timber 対応
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // シングルトン対応
        sInstance = this;
        try {
            Constructor<AndroidSingleton> constructor = AndroidSingleton.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            mAndroidSingleton = constructor.newInstance();
        } catch (NoSuchMethodException |
                IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            // never happen
        }
    }

    public static MainApplication getInstance() {
        return sInstance;
    }

    public AndroidSingleton getAndroidSingleton() {
        return mAndroidSingleton;
    }
}
