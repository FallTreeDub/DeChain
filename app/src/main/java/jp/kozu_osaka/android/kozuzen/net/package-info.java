/**
 * <p>
 *     DeChainデータベースとの通信に関係するクラス群。データベースとの通信にはHTTPプロトコルを用いる。
 * </p>
 * <h1>データのやり取りの方式</h1>
 * <p>
 *     通信の際、以下のようなデータのやり取りが行われる。これはDeChainで独自に取り決めたものである。
 * </p>
 * <pre>
 * HTTP GETアクセスする際のデータ方式
 *     ┌───────┐                                     ┌───────┐
 *     │       │      URLクエリにデータを載せる      │   デ  │
 *     │   ア  ┼─────────────────────────────────────┼►  ｜  │
 *     │   プ  │                                     │   タ  │
 *     │   リ  │                                     │   べ  │
 *     │       │                                     │   ｜  │
 *     │      ◄┼─────────────────────────────────────┼   ス  │
 *     └───────┘           応答内容(JSON)            └───────┘
 * </pre>
 * <br>
 * <pre>
 * HTTP POSTアクセスする際のデータ方式
 *     ┌───────┐                                     ┌───────┐
 *     │       │           POST内容(JSON)            │   デ  │
 *     │   ア  ┼─────────────────────────────────────┼►  ｜  │
 *     │   プ  │                                     │   タ  │
 *     │   リ  │                                     │   べ  │
 *     │       │                                     │   ｜  │
 *     │      ◄┼─────────────────────────────────────┼   ス  │
 *     └───────┘           応答内容(JSON)            └───────┘
 * </pre>
 *
 * <h1>データの内容</h1>
 * <h3>送信データについて</h3>
 * <p>
 *     DeChainデータベースに通信するためには、DeChainアプリはアプリ署名を送信する必要がある。
 *     アプリ署名が提示されない場合は、データベースは通信を拒否する。
 * </p>
 * <h3>受信データについて</h3>
 * <p>
 *     受信データには、レスポンスコードが必ず含まれる。レスポンスコードの一覧は以下のとおりである。
 *     (※アプリの不備ではなく、データベース内で起きたエラーは「(内部)」と表記)
 * </p>
 * <br>
 * 汎用
 * <table>
 *      <tr>   <th>レスポンスコード</th> <th>内容</th>   <th>レスポンスコード</th> <th>内容</th>   </tr>
 *      <tr>   <td>0</td> <td>エラーなし</td>   <td>-1</td> <td>エラーなし(メッセージあり)</td>   </tr>
 *      <tr>   <td>310</td ><td>引数にnullがある</td>   <td>311</td> <td>アプリ署名がない</td>   </tr>
 *      <tr>   <td>312</td> <td>RequestTypeのIDが不正</td>   <td>110</td> <td>すでに仮登録アカウントが登録されている</td>   </tr>
 *      <tr>   <td>120</td> <td>仮登録アカウントが見つからない</td>   <td>210</td> <td>すでに本登録アカウントが登録されている</td>   </tr>
 *      <tr>   <td>220</td> <td>本登録アカウントが見つからない</td>   </tr>
 * </table>
 * <br>
 * POSTリクエスト用
 * <table>
 *      <tr>   <th>レスポンスコード</th> <th>内容</th>   <th>レスポンスコード</th> <th>内容</th>   </tr>
 *      <tr>   <td>2101(内部)</td> <td>ConfirmResetPassAuthRequestで、DB上のpassResetRequestCode列が見つからなかった</td>   <td>2102</td> <td>ConfirmResetPassAuthRequestで、入力コードが間違っている</td>   </tr>
 *      <tr>   <td>2001</td> <td>ConfirmTentativeAuthRequestで、入力6桁コードが間違っている</td>   <td>3001(内部)</td> <td>RecreateResetPassAuthRequestで、DB上のpassResetRequestTimeもしくはpassResetRequestCode列が見つからなかった</td>   </tr>
 *      <tr>   <td>4001(内部)</td> <td>ResetPasswordRequestで、DB上のpassResetRequestTimeもしくはpassResetRequestCode列が見つからなかった</td>   <td>1201(内部)</td> <td>SendUsageDataRequestで、DBにデータを書き込めなかった</td>   </tr>
 *      <tr>   <td>1001(内部)</td> <td>RegisterTentativeRequestで、DBに書き込めなかった</td>   <td>1002[未使用]</td> <td>実験開始後の仮登録(当初禁止にする予定だったが、没。データベースからもこれは送られない)</td>   </tr>
 * </table>
 * <br>
 * GETリクエスト用
 * <table>
 *      <tr>   <th>レスポンスコード</th> <th>内容</th>   <th>レスポンスコード</th> <th>内容</th>   </tr>
 *      <tr>   <td>7001(内部)</td> <td>GetAverageOfUsageOneDayRequestで、DB上のtimeStamp列が見つからなかった</td>   <td>7003(内部)</td> <td>GetAverageOfUsageOneDayRequestで、DB上のtotalUsages列が見つからなかった</td>   </tr>
 *      <tr>   <td>6101(内部)</td> <td>GetRegisteredExistenceRequestで、DB上のpass列が見つからなかった</td>   <td>6102</td> <td>GetRegisteredExistenceRequestで、パスワードが間違っている</td>   </tr>
 * </table>
 *
 * @see <a href="https://qiita.com/kanfutrooper/items/6840046f17474a48b4b8">HTTP通信を基本からまとめてみた(Qiita)</a>
 * @see <a href="https://qiita.com/morikuma709/items/956d7c58908cb481d7e8">HTTPリクエストメソッドまとめ(Qiita)</a>
 * @see <a href="https://webtan.impress.co.jp/e/2012/04/26/12663">URLクエリパラメータ(クエリストリング)の意味とは。使い方は? 除外はすべき?(Web担当者Forum)</a>
 * @see <a href="https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads?hl=ja">Java スレッドによる非同期処理(Android Developers)</a>
 * @see <a href="https://qiita.com/yunity29/items/7ccc84d47e139340ecbc">非同期処理とは何か、何が嬉しいの？(Qiita)</a>
 * @see <a href="https://qiita.com/minateru/items/0fe791251dc008ed03a0">非同期処理と同期処理の違い(Qiita)</a>
 * @see <a href="https://qiita.com/WisteriaWave/items/dd6f74b24852438a90df">[Android]apkへの署名周りまとめ(Qiita)</a>
 *
 */
package jp.kozu_osaka.android.kozuzen.net;