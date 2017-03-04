package com.example.android;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * AspectJ を用いたログ出力クラス。
 *
 * @see <a href="https://github.com/uPhyca/gradle-android-aspectj-plugin">gradle-android-aspectj-plugin</a>
 * @see <a href="https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx">gradle_plugin_android_aspectjx</a>
 * @see <a href="https://github.com/JakeWharton/hugo/blob/master/hugo-runtime/src/main/java/hugo/weaving/internal/Hugo.java">Hugo.java</a>
 * @see <a href="https://hondou.homedns.org/pukiwiki/pukiwiki.php?AspectJ%20%A5%ED%A5%B0">AspectJ ログ</a>
 * @see <a href="https://www.slideshare.net/minoruchikamune/aspectjjava-20120907">AspectJによるJava言語拡張 2012.09.07</a>
 */
@Aspect
public class AspectLog {
    private static final String TAG = "AspectLog";

    @Pointcut("execution(* *(..)) && !within(AspectLog) && !within(Strings)")
    public void method() {
    }

    @Pointcut("execution(*.new(..)) && !within(AspectLog) && !within(Strings)")
    public void constructor() {
    }

    @Around("method() || constructor()")
    public Object logAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
        enterMethod(joinPoint);

        long startNanos = System.nanoTime();
        Object result = joinPoint.proceed();
        long stopNanos = System.nanoTime();
        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

        exitMethod(joinPoint, result, lengthMillis);

        return result;
    }

    //@Before("method() || constructor()")
    public void enterMethod(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        Class<?> cls = codeSignature.getDeclaringType();
        String fileName = joinPoint.getSourceLocation().getFileName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = codeSignature.getName();
        int lineNumber = joinPoint.getSourceLocation().getLine();
        String[] parameterNames = codeSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        StringBuilder builder = new StringBuilder("\u21E2 ");
        builder.append(className).append('#').append(methodName);
        builder.append('(').append(fileName).append(':').append(lineNumber).append(')');

        if (0 < parameterValues.length) {
            builder.append(" {");
            for (int i = 0; i < parameterValues.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(parameterNames[i]).append('=');
                builder.append(Strings.toString(parameterValues[i]));
            }
            builder.append('}');
        }

        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
        }

        Log.v(TAG, builder.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final String section = builder.toString().substring(2);
            Trace.beginSection(section);
        }
    }

    //@After("method() || constructor()")
    public void exitMethod(JoinPoint joinPoint) throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        String fileName = joinPoint.getSourceLocation().getFileName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = codeSignature.getName();
        int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
        boolean hasReturnType = codeSignature instanceof MethodSignature
                && ((MethodSignature) codeSignature).getReturnType() != void.class;

        StringBuilder builder = new StringBuilder("\u21E0 ");
        builder.append(className).append('#').append(methodName);
        builder.append('(').append(fileName).append(':').append(lineNumber).append(')');

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(((MethodSignature) codeSignature).getReturnType().getSimpleName());
        }

        Log.v(TAG, builder.toString());
    }

    private void exitMethod(JoinPoint joinPoint, Object result, long lengthMillis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }

        Signature signature = joinPoint.getSignature();

        Class<?> cls = signature.getDeclaringType();
        String fileName = joinPoint.getSourceLocation().getFileName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        int lineNumber = joinPoint.getSourceLocation().getLine();
        boolean hasReturnType = signature instanceof MethodSignature
                && ((MethodSignature) signature).getReturnType() != void.class;

        StringBuilder builder = new StringBuilder("\u21E0 ");
        builder.append(className).append('#').append(methodName);
        builder.append('(').append(fileName).append(':').append(lineNumber).append(')');
        builder.append(" [").append(lengthMillis).append("ms]");

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(Strings.toString(result));
        }

        Log.v(TAG, builder.toString());
    }
}
