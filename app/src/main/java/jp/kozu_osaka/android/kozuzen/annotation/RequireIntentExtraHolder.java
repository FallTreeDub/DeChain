package jp.kozu_osaka.android.kozuzen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link RequireIntentExtra}を複数個付与するために必要。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireIntentExtraHolder {
    RequireIntentExtra[] value();
}
