/**
 * DeChainのアップデートに使用するクラス群。
 * <h2>DeChainアップデートの処理の流れ</h2>
 * <img src="./javadoc_resource/aaaa.png" />
 * <li>{@link jp.kozu_osaka.android.kozuzen.net.update.DeChainUpDater#enqueueUpdate(Context)}が呼び出されると、{@code DownloadService}を起動する。</li>
 * <li>{@link jp.kozu_osaka.android.kozuzen.net.update.DownloadService}のダウンロード処理完了を、インスタンスフィールドの{@code downloadDoneReceiver}が検知、{@link jp.kozu_osaka.android.kozuzen.net.update.InstallService}を起動する。</li>
 * <li>{@link jp.kozu_osaka.android.kozuzen.net.update.InstallService}の処理が完了を{@link jp.kozu_osaka.android.kozuzen.net.update.DownloadExitReceiver}が検知する。</li>
 * <li>その他、{@link jp.kozu_osaka.android.kozuzen.net.update.DownloadService}やそのあとの{@code downloadDoneReceiver}、{@link jp.kozu_osaka.android.kozuzen.net.update.InstallService}の処理でエラーが起きた際は{@link jp.kozu_osaka.android.kozuzen.net.update.DownloadExitReceiver}がエラー処理のため途中で呼び出される。</li>
 * <li>(※それぞれの処理の詳しい内容はそれぞれのクラスのjavadocへ)</li>
 *
 * @see jp.kozu_osaka.android.kozuzen.net.update.DownloadService
 * @see jp.kozu_osaka.android.kozuzen.net.update.InstallService
 * @see jp.kozu_osaka.android.kozuzen.net.update.DownloadExitReceiver
 */
package jp.kozu_osaka.android.kozuzen.net.update;

import android.content.Context;