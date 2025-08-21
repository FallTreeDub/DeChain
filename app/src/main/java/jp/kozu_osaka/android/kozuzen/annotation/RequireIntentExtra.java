package jp.kozu_osaka.android.kozuzen.annotation;

import android.app.Activity;
import android.content.Intent;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Activity}を継承したクラスのなかで、{@link Intent}を使用しての
 * {@link Activity#startActivity(Intent)}での画面遷移の際に必要なExtraを知らせるアノテーション。
 * 複数Extraが必要な場合は、複数個このアノテーションを付与する。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(RequireIntentExtraHolder.class)
public @interface RequireIntentExtra {

    /**
     * 引き渡しの際にExtraとなるべきもののクラス。
     * @return IntentのExtraになるべきクラス。
     */
    Class<? extends Serializable> extraClazz();

    /**
     * 実際に{@link Intent}のExtraを引き渡す際、キーとして扱う文字列。
     * @return Extraのキーとなる文字列。
     */
    String extraKey();
}