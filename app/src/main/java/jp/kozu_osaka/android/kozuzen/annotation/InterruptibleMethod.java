package jp.kozu_osaka.android.kozuzen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Thread#interrupt()}で割り込みをされた際に、interruptを受けて
 * 動作を終了させることができるメソッドに付与するアノテーション。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface InterruptibleMethod {}