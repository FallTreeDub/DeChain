package jp.kozu_osaka.android.kozuzen.access.task;

import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;

/**
 * <p>
 *     SpreadSheetへのアクセスで、具体的にどのようなアクセスをするのかを規定するクラス。
 *     アクセス処理は{@link AccessTask#run(Sheets.Spreadsheets)}で行う。
 * </p>
 */
public abstract class AccessTask {

    /**
     * Spreadsheetを介したアクセスを行う。
     * @return アクセス結果。{@link jp.kozu_osaka.android.kozuzen.access.AccessThread}経由での
     * アクセスで、処理途中にinterruptされた際は{@code null}を返す。
     * @exception IOException Spreadsheetアクセス中に起きたIOException。
     * @exception ExecutionException SpreadSheetアクセス中に起きたExecutionException。
     * {@link java.util.concurrent.ExecutorService}実行時の{@link Future#get()}によって引き起こされうる例外。
     * @annotation {@link InterruptibleMethod}
     */
    @InterruptibleMethod
    public abstract AccessResult run() throws ExecutionException, IOException;

    /**
     * 処理結果が得られずtimeoutとなったとき。
     */
    public abstract void whenTimeOut();

    /**
     * アクセスに成功したときの処理。
     */
    public abstract void whenSuccess();

    /**
     * アクセスに失敗したときの処理。例外は発生していないが、入力値の不備などでアクセスが継続できない場合。
     * @param failureResult メッセージにアクセス失敗の理由を表記している、{@link AccessResult.Failure}
     */
    public abstract void whenFailed(AccessResult.Failure failureResult);

    /**
     * アクセス処理中にエラーが発生したときの処理。
     * コーディングの不備によって例外が発生した場合に呼び出される。
     * @param e 発生した例外。
     */
    public abstract void whenError(Exception e);
}
