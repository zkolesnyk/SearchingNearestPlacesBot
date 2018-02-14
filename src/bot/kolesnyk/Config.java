package bot.kolesnyk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static final String CONFIG_BOT_FILE = "./config/bot/bot.properties";

    public static String BOT_NAME;
    public static String BOT_TOKEN;

    public static void load() {
        Properties botSettings = new Properties();

        try (InputStream is = new FileInputStream(new File(CONFIG_BOT_FILE))) {
            botSettings.load(is);
            is.close();
            System.out.println("Настройки бота успешно загружены");
        } catch (Exception e) {
            System.out.println("Ошибка загрузки настроек бота");
        }

        BOT_NAME = botSettings.getProperty("botName", "kolesnyk_bot");
        BOT_TOKEN = botSettings.getProperty("botToken", "Ha-ha-ha");
    }
}
