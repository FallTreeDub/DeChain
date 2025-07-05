package jp.kozu_osaka.android.kozuzen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * このアノテーションは、HTTP接続などの、AndroidシステムがUIメソッド上で動作させることをサポートしていない
 * 処理を行うメソッドに対して付与するべきもの。
 * <blockquote><pre>
 *      //Example for using @RunOnSubMethod
 *      {@literal @}RunOnSubMethod
 *      public void doSomethingOnSub() {
 *          //Doing something...
 *      }
 * </pre></blockquote>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface RunOnSubMethod {}