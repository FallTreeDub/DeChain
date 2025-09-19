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
import java.util.Locale;
import java.util.Map;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.data.UsageData;
import jp.kozu_osaka.android.kozuzen.data.DailyUsageDatas;
import jp.kozu_osaka.android.kozuzen.internal.exception.DateIsInvalidException;
import jp.kozu_osaka.android.kozuzen.internal.exception.JsonIsNotEmptyException;
import jp.kozu_osaka.android.kozuzen.internal.exception.UsageDataAlreadyExistsException;

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

    /**
     * 新しく一か月分のデータを格納できるようにjsonの内容を整形する。
     * すでにjsonに何かしらの値が書き込まれている場合は{@link JsonIsNotEmptyException}
     * が投げられる。
     * @param calendar 集計対象となる年月。
     * @throws JsonIsNotEmptyException jsonが空でない場合。
     * @throws IOException jsonの読み込みにエラーが発生した場合。
     */
    public static void init(Calendar calendar) throws JsonIsNotEmptyException, IOException {
        if(Files.readAllBytes(JSON_PATH).length == 0) throw new JsonIsNotEmptyException("The json for storing data of app usage is not empty.");

        try(JsonWriter jsonWriter = new JsonWriter(new FileWriter(JSON_PATH.toFile()))) {
            jsonWriter.name(KEY_YEAR).value(calendar.get(Calendar.YEAR));
            jsonWriter.name(KEY_MONTH).value(calendar.get(Calendar.DAY_OF_MONTH));
        } //IOExceptionは上位に投げる
    }

    /**
     * 1日ごとのSNS、ゲームアプリ使用時間をjsonに格納する。
     * @param datas
     */
    public static void addDailyDatas(DailyUsageDatas datas) throws IOException, DateIsInvalidException {
        try(FileReader reader = new FileReader(JSON_PATH.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if(getDataOf(datas.getDayOfMonth()) != null) throw new UsageDataAlreadyExistsException("The usage data on specified day already exists.");
            JsonArray datasArray = root.getAsJsonArray(KEY_DATAS);
            if(datasArray == null) {
                datasArray = new JsonArray();
                root.add(KEY_DATAS, datasArray);
            }

            //Create a new JsonObject from the instance of DailyUsageDatas
            JsonObject newJsonData = new JsonObject();
            newJsonData.addProperty(KEY_DATA_SNS_ALL, String.format(Locale.JAPAN, "%d:%d", datas.getSNSHours(), datas.getSNSMinutes()));
            newJsonData.addProperty(KEY_DATA_GAMES_ALL, String.format(Locale.JAPAN, "%d:%d", datas.getGamesHours(), datas.getGamesMinutes()));
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
                addTargetDataObject.addProperty(d.getAppName(), String.format(Locale.JAPAN, "%d:%d", d.getUsageHours(), d.getUsageMinutes()));
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

    /**
     * 保存された一か月分のデータを消去する。
     * @throws IOException 削除にエラーが発生した場合にスローされる。
     */
    public static void eraseDatas() throws IOException {
        try(FileChannel c = FileChannel.open(JSON_PATH, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            c.truncate(0L); //サイズを0に切り詰める
        } //IOExceptionは上位に投げる
    }

    /**
     *
     */
    private static final class DailyUsageDatasDeserializer implements JsonDeserializer<DailyUsageDatas> {

        //UsageDataのdeserializeには、Gsonでdeserializer選択してdeserialize

        @Override
        public DailyUsageDatas deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, IllegalArgumentException {
            JsonObject obj = (JsonObject)json;
            int dayOfMonth = obj.get(KEY_DATA_DAY_OF_MONTH).getAsInt();
            DailyUsageDatas instance = DailyUsageDatas.create(dayOfMonth);
            if(instance == null) throw new IllegalArgumentException("The dayOfMonth field in the json is invalid.");
            JsonObject snsDatasJson = obj.get(KEY_DATA_DATA_OF_SNS).getAsJsonObject();
            for(Map.Entry<String, JsonElement> e : snsDatasJson.entrySet()) {
                String appName = e.getKey();
                String usageTimeStr = e.getValue().getAsString();
                int usageHours = Integer.parseInt(usageTimeStr.split(":")[0]);
                int usageMinutes = Integer.parseInt(usageTimeStr.split(":")[1]);
                UsageData oneAppUsage = new UsageData(UsageData.AppType.SNS, appName, usageHours, usageMinutes);
                instance.add(oneAppUsage);
            }

            JsonObject gamesDatasJson = obj.get(KEY_DATA_DATA_OF_GAMES).getAsJsonObject();
            for(Map.Entry<String, JsonElement> e : gamesDatasJson.entrySet()) {
                String appName = e.getKey();
                String usageTimeStr = e.getValue().getAsString();
                int usageHours = Integer.parseInt(usageTimeStr.split(":")[0]);
                int usageMinutes = Integer.parseInt(usageTimeStr.split(":")[1]);
                UsageData oneAppUsage = new UsageData(UsageData.AppType.SNS, appName, usageHours, usageMinutes);
                instance.add(oneAppUsage);
            }
            return instance;
        }
    }

    /**
     * {@link UsageData}が保持するアプリのタイプ(SNS, Games)をjsonからデシリアライズする際に使用する。
     * json上は、アプリのタイプはint型の数値で格納されているため、このクラスを用いて{@link jp.kozu_osaka.android.kozuzen.data.UsageData.AppType}
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
