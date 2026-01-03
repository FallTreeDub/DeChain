/**
 * データベースへのスマホの利用時間データの送信に関係するクラス群。
 * <p>
 *     本登録アカウント登録時に、{@link jp.kozu_osaka.android.kozuzen.net.usage.UsageDataBroadcastReceiver}が現在時刻から数えて次の8時に
 *     起動するよう予約(pend)される。
 * </p>
 * <p>
 *     アカウントからログアウトされた時や、内部アカウントを使用しての自動ログイン時にエラーが発生した際には予約が外れる。
 * </p>
 * <p>
 *     {@link jp.kozu_osaka.android.kozuzen.net.usage.UsageDataBroadcastReceiver}はじめ{@link android.content.BroadcastReceiver}は
 *     Androidデバイスの再起動で予約(pend)が外れてしまうので、{@link jp.kozu_osaka.android.kozuzen.net.usage.RePendingBroadcastReceiver}に
 *     再起動を検知させ、{@link jp.kozu_osaka.android.kozuzen.net.usage.UsageDataBroadcastReceiver}を再予約する。
 * </p>
 *
 * @see <a href="https://developer.android.com/develop/background-work/background-tasks/broadcasts?hl=ja">ブロードキャストの概要(Android Developers)</a>
 * @see <a href="https://qiita.com/jabberwocky3376/items/d39f8d04e3e0ef395625">【Android】 Broadcast Recieverでintentを受け取る(Qiita)</a>
 * @see <a href="https://android.keicode.com/basics/services-communicate-broadcast-receiver.php">ブロードキャストレシーバの実装によるアクティビティとサービスの通信(Android 開発入門)</a>
 */
package jp.kozu_osaka.android.kozuzen.net.usage;