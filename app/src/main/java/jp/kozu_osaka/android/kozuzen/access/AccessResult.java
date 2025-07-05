package jp.kozu_osaka.android.kozuzen.access;

import androidx.annotation.StringRes;

import jp.kozu_osaka.android.kozuzen.KozuZen;

/**
 * SpreadSheetへのアクセスの結果。
 */
public class AccessResult {

    private AccessResult() {}

    public static class Builder {
        /**
         * SpreadSheetへのアクセスに成功した時のアクセス結果の実体。
         * @return 「成功した」と知らせるアクセス結果の実体。
         */
        public static AccessResult.Success success() {
            return new AccessResult.Success();
        }

        /**
         * SpreadSheetへのアクセスに失敗した時のアクセス結果の実体。
         * @param message 通知に、アクセス失敗の理由などを表示する際のメッセージ。
         * @return 「失敗した」と知らせるアクセス結果の実体。
         */
        public static AccessResult.Failure failure(String message) {
            return new AccessResult.Failure(message);
        }

        public static AccessResult.Failure failure(@StringRes int messageId) {
            return failure(KozuZen.getInstance().getString(messageId));
        }
    }

    public static class Success extends AccessResult {
        private Success() {}
    }

    public static class Failure extends AccessResult {
        private final String MESSAGE_ON_NOTIFICATION;

        private Failure(String messageOnNotification) {
            this.MESSAGE_ON_NOTIFICATION = messageOnNotification;
        }

        /**
         * 通知に、アクセス失敗の理由などを表示する際のメッセージ。
         * @return メッセージ本体。
         */
        public String getMessage() {
            return MESSAGE_ON_NOTIFICATION;
        }
    }
}