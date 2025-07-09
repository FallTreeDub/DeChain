package jp.kozu_osaka.android.kozuzen.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.data.AppTypeDeserializer;
import jp.kozu_osaka.android.kozuzen.data.UsageData;
import jp.kozu_osaka.android.kozuzen.data.DailyUsageDatas;

/**
 * 内部ストレージにjsonとして格納した1か月分のSNS、ゲームアプリ利用データ。
 */
public class InternalUsageDataManager {

    private static final Path JSON_PATH = KozuZen.getInstance().getFilesDir().toPath().resolve("internalUsageDatas.json");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(UsageData.AppType.class, new AppTypeDeserializer())
            .create();

    private InternalUsageDataManager() {}

    /**
     * 1日ごとのSNS、ゲームアプリ使用時間をjsonに格納する。
     * @param datas
     */
    public static void addDailyData(DailyUsageDatas datas) throws IOException {
        List<DailyUsageDatas> dataList = new ArrayList<>();
        try(FileReader reader = new FileReader(JSON_PATH.toFile())) {
            Type listType = new TypeToken<List<DailyUsageDatas>>(){}.getType();
            if(GSON.fromJson(reader, listType) != null) {
                dataList = GSON.fromJson(reader, listType);
            }
        } //IOExceptionは上部に投げる
        dataList.add(datas);

        try(FileWriter w = new FileWriter(JSON_PATH.toFile())) {
            GSON.toJson(dataList, w);
        }
    }

    /**
     *
     * @param dayOfMonth
     * @return
     * @throws IOException
     */
    public static DailyUsageDatas getDataOf(int dayOfMonth) throws IOException {
        List<DailyUsageDatas> dataList = new ArrayList<>();
        try(FileReader r = new FileReader(JSON_PATH.toFile())) {
            Type listType = new TypeToken<List<DailyUsageDatas>>(){}.getType();
            if(GSON.fromJson(r, listType) != null) {
                dataList = GSON.fromJson(r, listType);
            }
        }

        for(DailyUsageDatas d : dataList) {
            if(d.getDayOfMonth() == dayOfMonth) {
                return d;
            }
        }
        return null;
    }

    /**
     * 保存された一か月分のデータを消去する。
     */
    public static void eraseDatas() {
        try(FileChannel c = FileChannel.open(JSON_PATH, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            c.truncate(0L); //サイズを0に切り詰める
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
