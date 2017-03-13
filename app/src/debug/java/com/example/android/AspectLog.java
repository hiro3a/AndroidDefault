package com.example.android;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * AspectJ を用いたログ出力クラス。
 *
 * @see <a href="https://github.com/uPhyca/gradle-android-aspectj-plugin">gradle-android-aspectj-plugin</a>
 * @see <a href="https://github.com/HujiangTechnology/gradle_plugin_android_aspectjx">gradle_plugin_android_aspectjx</a>
 * @see <a href="https://github.com/JakeWharton/hugo/blob/master/hugo-runtime/src/main/java/hugo/weaving/internal/Hugo.java">Hugo.java</a>
 * @see <a href="https://hondou.homedns.org/pukiwiki/pukiwiki.php?AspectJ%20%A5%ED%A5%B0">AspectJ ログ</a>
 * @see <a href="https://www.slideshare.net/minoruchikamune/aspectjjava-20120907">AspectJによるJava言語拡張 2012.09.07</a>
 */
@SuppressWarnings("unused")
@Aspect
public class AspectLog {
    private static final String TAG = "AspectLog";

    @Pointcut("execution(* *(..)) && !execution(* set*(..)) && !execution(* get*(..)) && !execution(* toString(..)) && !within(AspectLog) && !within(Strings)")
    public void method() {
    }

    @Pointcut("execution(*.new(..)) && !within(AspectLog) && !within(Strings)")
    public void constructor() {
    }


    /**
     * メソッド呼び出し時のログ出力。
     * @param joinPoint ジョインポイント
     */
    @Before("method() || constructor()")
    public void beforeMethod(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        Class<?> cls = codeSignature.getDeclaringType();
        String fileName = joinPoint.getSourceLocation().getFileName();
        String className = getClassName(cls);
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

    /**
     * メソッドの実行が正常終了したときのログ出力。
     * @param joinPoint ジョインポイント
     * @param result 戻り値
     */
    @AfterReturning(pointcut = "method() || constructor()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }

        Signature signature = joinPoint.getSignature();

        Class<?> cls = signature.getDeclaringType();
        String fileName = joinPoint.getSourceLocation().getFileName();
        String className = getClassName(cls);
        String methodName = signature.getName();
        int lineNumber = getLineNumber();
        boolean hasReturnType = signature instanceof MethodSignature
                && ((MethodSignature) signature).getReturnType() != void.class;

        StringBuilder builder = new StringBuilder("\u21E0 ");
        builder.append(className).append('#').append(methodName);
        builder.append('(').append(fileName).append(':').append(lineNumber).append(')');

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(Strings.toString(result));
        }

        Log.v(TAG, builder.toString());
    }

    /**
     * 例外が発生したときのログ出力。
     * @param joinPoint ジョインポイント
     * @param exception 例外
     */
    @AfterThrowing(pointcut = "method() || constructor()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }

        Signature signature = joinPoint.getSignature();

        Class<?> cls = signature.getDeclaringType();
        String fileName = joinPoint.getSourceLocation().getFileName();
        String className = getClassName(cls);
        String methodName = signature.getName();
        int lineNumber = getLineNumber(joinPoint, exception);
        String exceptionName = exception.getClass().getName();

        StringBuilder builder = new StringBuilder("\u21E0 ");
        builder.append(className).append('#').append(methodName);
        builder.append('(').append(fileName).append(':').append(lineNumber).append(')');
        builder.append(" [").append(exceptionName).append(']');

        Log.e(TAG, builder.toString());
    }

    /**
     * ログ出力用のクラス名を得ます。
     * @param cls 対象のクラス
     * @return クラス名
     */
    private static String getClassName(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            return getClassName(cls.getEnclosingClass());
        }
        return cls.getSimpleName();
    }

    /**
     * 対象のポイントカットが呼び出されたタイミングのソースコードの行番号を得ます。
     * @return 行番号
     */
    private static int getLineNumber() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        int lineNumber = elements[4].getLineNumber();
        return lineNumber;
    }

    /**
     * {@code Exception} のスタックトレースから、対象のポイントカットが呼び出されたタイミングのソースコードの行番号を得ます。
     * @param joinPoint ジョインポイント
     * @param exception 例外
     * @return 行番号
     */
    private static int getLineNumber(JoinPoint joinPoint, Exception exception) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        String fileName = element.getFileName();
        String className = element.getClassName();
        String methodName = element.getMethodName();
        int topLineNumber = joinPoint.getSourceLocation().getLine();
        int endLineNumber = element.getLineNumber();

        for (StackTraceElement trace : exception.getStackTrace()) {
            int lineNumber = trace.getLineNumber();
            if (fileName.equals(trace.getFileName()) &&
                    className.equals(trace.getClassName()) &&
                    methodName.equals(trace.getMethodName()) &&
                    topLineNumber < trace.getLineNumber() &&
                    trace.getLineNumber() <= endLineNumber) {
                return lineNumber;
            }
        }
        return endLineNumber;
    }
}
