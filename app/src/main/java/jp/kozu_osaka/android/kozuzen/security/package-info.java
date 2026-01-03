/**
 * 暗号化や秘匿すべき情報の格納、入力情報の確認に使用するクラス群。
 * 詳しい説明は各々のクラスにて記している。
 *
 * <h2>Secrets.javaの扱いについて</h2>
 * <p>
 *     Secrets.javaは、データベースへのアクセス用URLなど秘匿すべき情報を持つため、<u><b>絶対に外部に漏らしてはならない。</b></u>
 *     一般に、このような秘匿情報を扱う際には、ソースコードに直書きするのではなく、別ファイルにすべてまとめて管理するべきである。
 * </p>
 *
 * @see <a href="https://qiita.com/watabee/items/c393f19672b96b34b0ef">[Android]シークレットキーなどの秘匿情報をプロジェクトで使うための設定(Qiita)</a>
 */
package jp.kozu_osaka.android.kozuzen.security;