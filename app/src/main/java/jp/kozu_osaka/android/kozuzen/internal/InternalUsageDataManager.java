package jp.kozu_osaka.android.kozuzen.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.net.usage.data.DailyUsageDatas;
import jp.kozu_osaka.android.kozuzen.net.usage.data.UsageData;

/**
 * 内部ストレージにjsonとして格納した1か月分のSNS、ゲームアプリ利用データ。
 */
public final class InternalUsageDataManager {

    private static final Path JSON_PATH = KozuZen.getInstance().getFilesDir().toPath().resolve("internalUsageDatas.json");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(UsageData.AppType.class, new AppTypeDeserializer())
            .registerTypeAdapter(DailyUsageDatas.class, new DailyUsageDatasDeserializer())
            .create();
    private static final String KEY_YEAR = "year";
    private static final String KEY_MONTH = "month";
    private static final String KEY_DATAS = "datas";

    private static final String KEY_DATA_SNS_ALL = "SNS_ALL";
    private static final String KEY_DATA_GAMES_ALL = "GAMES_ALL";
    private static final String KEY_DATA_DAY_OF_MONTH = "dayOfMonth";
    private static final String KEY_DATA_DATA_OF_SNS = "SNS";
    private static final String KEY_DATA_DATA_OF_GAMES = "games";

    private InternalUsageDataManager() {}

    private static void init() throws IOException {
        boolean need = false;
        if(Files.notExists(JSON_PATH) || Files.size(JSON_PATH) == 0) {
            need = true;
        } else {
            int regedMonth;
            try(FileReader reader = new FileReader(JSON_PATH.toFile())) {
                JsonElement rootElem = JsonParser.parseReader(reader);
                if(rootElem == null || !rootElem.isJsonObject()) {
                    need = true;
                } else {
                    regedMonth = rootElem.getAsJsonObject().get(KEY_MONTH).getAsInt();
                    if(Calendar.getInstance(Locale.JAPAN).get(Calendar.MONTH) != regedMonth) {
                        need = true;
                    }
                }
            } catch(Exception e) { //jsonが壊れている場合
                need = true;
            }
        }

        if(need) {
            if(Files.exists(JSON_PATH)) {
                Files.write(JSON_PATH, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            }

            Calendar today = Calendar.getInstance(Locale.JAPAN);
            JsonObject root = new JsonObject();
            root.addProperty(KEY_YEAR, today.get(Calendar.YEAR));
            root.addProperty(KEY_MONTH, today.get(Calendar.MONTH) + 1);
            root.add(KEY_DATAS, new JsonArray());

            try(FileWriter writer = new FileWriter(JSON_PATH.toFile())) {
                GSON.toJson(root, writer);
            }
        }
    }

    public static void addDailyDatas(@NotNull DailyUsageDatas datas) throws IOException, IllegalArgumentException {
        init();

        //同じ日付あるなら上書き
        if(getDataOf(datas.getDayOfMonth()) != null) {
            removeDailyDatasOf(datas.getDayOfMonth());
        }

        try(FileReader reader = new FileReader(JSON_PATH.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray datasArray = root.getAsJsonArray(KEY_DATAS);

            //Create a new JsonObject from the instance of DailyUsageDatas
            JsonObject newJsonData = new JsonObject();
            newJsonData.addProperty(KEY_DATA_SNS_ALL, String.valueOf(datas.getSNSMinutes()));
            newJsonData.addProperty(KEY_DATA_GAMES_ALL, String.valueOf(datas.getGamesMinutes()));
            newJsonData.addProperty(KEY_DATA_DAY_OF_MONTH, datas.getDayOfMonth());
            JsonObject snsData = new JsonObject();
            JsonObject gamesData = new JsonObject();
            for(UsageData d : datas.getUsageDatas()) {
                JsonObject addTargetDataObject;
                switch(d.getAppType()) {
                    case SNS:
                        addTargetDataObject = snsData;
                        break;
                    case GAMES:
                        addTargetDataObject = gamesData;
                        break;
                    default:
                        continue;
                }
                addTargetDataObject.addProperty(d.getAppName(), String.valueOf(d.getUsageMinutes()));
            }
            newJsonData.add(KEY_DATA_DATA_OF_SNS, snsData);
            newJsonData.add(KEY_DATA_DATA_OF_GAMES, gamesData);

            datasArray.add(newJsonData);

            //ファイルに変更状況書き込み
            try(FileChannel channel = FileChannel.open(JSON_PATH,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DSYNC);
                OutputStreamWriter writer = new OutputStreamWriter(
                        Channels.newOutputStream(channel), StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }

        } //IOExceptionは上位に投げる
    }

    public static void removeDailyDatasOf(@Range(from = 1, to = 31) int dayOfMonth) throws IOException {
        if(getDataOf(dayOfMonth) == null) return;

        try(FileReader reader = new FileReader(JSON_PATH.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray datasArray = root.getAsJsonArray(KEY_DATAS);
            if(datasArray == null) return;
            for(JsonElement dataElem : datasArray) {
                if(dataElem.isJsonObject()) {
                    JsonObject dataObj = dataElem.getAsJsonObject();
                    if(dataObj.get(KEY_DATA_DAY_OF_MONTH).getAsInt() > dayOfMonth) {
                        break;
                    }
                    if(dataObj.get(KEY_DATA_DAY_OF_MONTH).getAsInt() == dayOfMonth) {
                        datasArray.remove(dataElem);
                    }
                }
            }

            //ファイルに変更状況書き込み
            try(FileChannel channel = FileChannel.open(JSON_PATH,
                    StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
                OutputStreamWriter writer = new OutputStreamWriter(
                        Channels.newOutputStream(channel), StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
                writer.flush();
                channel.force(true);
            }
        }
    }

    /**
     * @param dayOfMonth
     * @return 存在しない場合はnullが返される。
     * @throws IOException
     */
    public static DailyUsageDatas getDataOf(int dayOfMonth) throws IOException {
        if(!(1 <= dayOfMonth && dayOfMonth <= 31)) {
            return null;
        }
        if(Files.notExists(JSON_PATH)) {
            return null;
        }

        try(FileReader reader = new FileReader(JSON_PATH.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray datasArray = root.getAsJsonArray(KEY_DATAS);
            if(datasArray == null) return null;

            for(JsonElement dataEle : datasArray.asList()) {
                if(!(dataEle instanceof JsonObject)) continue;
                JsonObject obj = (JsonObject)dataEle;
                if(obj.get(KEY_DATA_DAY_OF_MONTH).getAsInt() == dayOfMonth) {
                    return GSON.fromJson(obj, DailyUsageDatas.class);
                }
            }
        } //IOExceptionは上位に投げる
        return null;
    }

    private static final class DailyUsageDatasDeserializer implements JsonDeserializer<DailyUsageDatas> {

        @Override
        public DailyUsageDatas deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, IllegalArgumentException {
            JsonObject obj = (JsonObject)json;
            int dayOfMonth = obj.get(KEY_DATA_DAY_OF_MONTH).getAsInt();
            DailyUsageDatas instance = DailyUsageDatas.create(dayOfMonth);
            JsonObject snsDatasJson = obj.get(KEY_DATA_DATA_OF_SNS).getAsJsonObject();
            for(Map.Entry<String, JsonElement> e : snsDatasJson.entrySet()) {
                String appName = e.getKey();
                UsageData oneAppUsage = new UsageData(UsageData.AppType.SNS, appName, TimeUnit.MINUTES.toMillis(Integer.parseInt(e.getValue().getAsString())));
                instance.add(oneAppUsage);
            }

            JsonObject gamesDatasJson = obj.get(KEY_DATA_DATA_OF_GAMES).getAsJsonObject();
            for(Map.Entry<String, JsonElement> e : gamesDatasJson.entrySet()) {
                String appName = e.getKey();
                UsageData oneAppUsage = new UsageData(UsageData.AppType.GAMES, appName, TimeUnit.MINUTES.toMillis(Integer.parseInt(e.getValue().getAsString())));
                instance.add(oneAppUsage);
            }
            return instance;
        }
    }

    /**
     * {@link UsageData}が保持するアプリのタイプ(SNS, Games)をjsonからデシリアライズする際に使用する。
     * json上は、アプリのタイプはint型の数値で格納されているため、このクラスを用いて{@link UsageData.AppType}
     * オブジェクトにデシリアライズすることが必要。
     */
    private static final class AppTypeDeserializer implements JsonDeserializer<UsageData.AppType> {

        @Override
        public UsageData.AppType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            int id = json.getAsInt();
            return UsageData.AppType.from(id);
        }
    }
}
