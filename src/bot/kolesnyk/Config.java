package bot.kolesnyk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static final String CONFIG_BOT_FILE = "./config/bot/bot.properties";
    public static final String CONFIG_DB_FILE = "./config/database/database.properties";

    public static String BOT_NAME;
    public static String BOT_TOKEN;
    public static String GOOGLE_PLACES_API_TOKEN;

    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PWD;

    public static void load() {
        Properties botSettings = new Properties();

        try (InputStream is = new FileInputStream(new File(CONFIG_BOT_FILE))) {
            botSettings.load(is);
            is.close();
            System.out.println("Настройки бота успешно загружены");
        } catch (Exception e) {
            System.out.println("Ошибка загрузки настроек бота");
        }

        Properties dbSettings = new Properties();

        try (InputStream is = new FileInputStream(new File(CONFIG_DB_FILE))) {
            dbSettings.load(is);
            is.close();
            System.out.println("Настройки БД успешно загружены");
        } catch (Exception e) {
            System.out.println("Ошибка загрузки настроек БД");
        }

        BOT_NAME = botSettings.getProperty("botName", "kolesnyk_bot");
        BOT_TOKEN = botSettings.getProperty("botToken", "Ha-ha-ha");
        GOOGLE_PLACES_API_TOKEN = botSettings.getProperty("googlePlacesApiToken", "Ha-ha-ha");

        DB_URL = dbSettings.getProperty("dbUrl", "");
        DB_USER = dbSettings.getProperty("dbUser", "");
        DB_PWD = dbSettings.getProperty("dbPwd", "");
    }
}
