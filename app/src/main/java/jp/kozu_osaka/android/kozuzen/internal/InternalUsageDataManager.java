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
import com.google.gson.stream.JsonWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.net.usage.data.UsageData;
import jp.kozu_osaka.android.kozuzen.net.usage.data.DailyUsageDatas;

/**
 * 内部ストレージにjsonとして格納した1か月分のSNS、ゲームアプリ利用データ。
 */
public final class InternalUsageDataManager {

    private static final Path JSON_PATH = KozuZen.getInstance().getFilesDir().toPath().resolve("internalUsageDatas.json");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(UsageData.AppType.class, new InternalUsageDataManager.AppTypeDeserializer())
            .registerTypeAdapter(DailyUsageDatas.class, new InternalUsageDataManager.DailyUsageDatasDeserializer())
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

    /**
     * @throws IOException jsonの読み込みにエラーが発生した場合。
     */
    private static void init() throws IOException {
        if(Files.notExists(JSON_PATH)) {
            Calendar today = Calendar.getInstance();
            try(JsonWriter jsonWriter = new JsonWriter(new FileWriter(JSON_PATH.toFile()))) {
                jsonWriter.name(KEY_YEAR).value(today.get(Calendar.YEAR));
                jsonWriter.name(KEY_MONTH).value(today.get(Calendar.DAY_OF_MONTH));
            } //IOExceptionは上位に投げる
        }
    }

    /**
     * 1日ごとのSNS、ゲームアプリ使用時間をjsonに格納する。
     * @param datas
     */
    public static void addDailyDatas(DailyUsageDatas datas) throws IOException, IllegalArgumentException {
        if(getDataOf(datas.getDayOfMonth()) != null) throw new IllegalArgumentException("The usage data on specified day already exists.");

        init();

        try(FileReader reader = new FileReader(JSON_PATH.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray datasArray = root.getAsJsonArray(KEY_DATAS);
            if(datasArray == null) {
                datasArray = new JsonArray();
                root.add(KEY_DATAS, datasArray);
            }

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
        } //IOExceptionは上位に投げる
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
            if(instance == null) throw new IllegalArgumentException("The dayOfMonth field in the json is invalid.");
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
