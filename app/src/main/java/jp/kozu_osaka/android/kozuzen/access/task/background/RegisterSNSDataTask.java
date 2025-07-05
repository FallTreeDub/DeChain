package jp.kozu_osaka.android.kozuzen.access.task.background;

import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import jp.kozu_osaka.android.kozuzen.Constants;
import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.R;
import jp.kozu_osaka.android.kozuzen.access.AccessResult;
import jp.kozu_osaka.android.kozuzen.annotation.InterruptibleMethod;
import jp.kozu_osaka.android.kozuzen.annotation.RunOnSubMethod;

public class RegisterSNSDataTask extends BackgroundAccessTask {

    private final String targetMailAddress;

    public RegisterSNSDataTask(String targetMailAddress) {
        this.targetMailAddress = targetMailAddress;
    }

    @Override
    @RunOnSubMethod
    @InterruptibleMethod
    public AccessResult run() throws ExecutionException, IOException {

        return AccessResult.Builder.success();

        /*
        ValueRange registeredMailLine = sheet.values().get(
                        Constants.System.SPREADSHEET_ID,
                        Constants.System.SPREADSHEET_SHEET_NAME_SNS_DATAS + "!A2:A")
                .execute();
        if(registeredMailLine.getValues() == null) return AccessResult.FAILURE;

        Integer targetMailAddressRow = null;
        //lineは縦に並んだ列のうちの一つ一つの列。
        for(int row = 2; row <= registeredMailLine.getValues().size(); row++) {
            String registeredMailAddress = (String)registeredMailLine.getValues().get(row - 2).get(0);
            if(registeredMailAddress.isEmpty()) continue;
            if(registeredMailAddress.equals(this.targetMailAddress)) {
                targetMailAddressRow = row;
                break;
            }
        }
        if(targetMailAddressRow == null) return AccessResult.FAILURE;

        ValueRange dateLine = sheet.values().get(
                        Constants.System.SPREADSHEET_ID,
                        Constants.System.SPREADSHEET_SHEET_NAME_SNS_DATAS + "!B1:1")
                .execute();
        if(dateLine == null) return AccessResult.FAILURE;

        Calendar todayCalendar = Calendar.getInstance();
        int todayMonth = todayCalendar.get(Calendar.MONTH) + 1;
        int todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = String.format(Locale.JAPAN, "%d/%d", todayMonth, todayDay);
        Integer storeColumn = null;
        for(int i = 0; i < dateLine.getValues().get(0).size(); i++) {
            String date = (String)dateLine.getValues().get(0).get(i);
            if(date.equals(todayDate)) {
                storeColumn = i + 1;
                break;
            }
        }

        //今日の日付が見つからないとき
        if(storeColumn == null) {
            ValueRange body = new ValueRange().setValues(Collections.singletonList(Collections.singletonList(todayDate)));
            sheet.values()
                    .append(Constants.System.SPREADSHEET_ID,
                            Constants.System.SPREADSHEET_SHEET_NAME_SNS_DATAS + "!R1C" + dateLine.getValues().get(0).size(),
                            body)//<- todo: 内容をAppUsageStatから持ってくる
                    .setInsertDataOption("INSERT_ROWS")
                    .setValueInputOption("RAW")
                    .execute();
            storeColumn = dateLine.getValues().get(0).size();
        }

        ValueRange snsData = new ValueRange().setValues(Collections.singletonList(Collections.singletonList("aaa")));//<- todo: 内容をAppUsageStatから持ってくる
        sheet.values()
                .append(Constants.System.SPREADSHEET_ID,
                        Constants.System.SPREADSHEET_SHEET_NAME_SNS_DATAS + "!R" + targetMailAddressRow + "C" + storeColumn,
                        snsData)
                .setInsertDataOption("INSERT_ROWS")
                .setValueInputOption("RAW")
                .execute();
        return AccessResult.SUCCESS;*/
    }

    @Override
    public void whenTimeOut() {

    }
}
