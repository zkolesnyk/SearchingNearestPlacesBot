package bot.kolesnyk;

import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {

    Map locations = new HashMap<Long, Location>();

    public static void main(String[] args) {
        Config.load();
        ApiContextInitializer.init(); // инициализируем api
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        // костяк с ответами на сообщения
        Message message = update.getMessage();
        String text = message.getText();
        String searchPlace = "";
        String searchType = "";
        boolean searchOpenNow = true;
        if (message.hasText()) {
            switch (text) {
                case "/start":
                    sendMsg(message, "Привет!");
                    sendLoc(message, "Отправь, пожалуйста, свои координаты:");
                    break;
                case "/loc":
                    sendLoc(message, "Отправь, пожалуйста, свои координаты:");
                    break;
                case "/lord":
                    sendPht(message);
                    break;
                case "туалет":
                    searchPlace = "туалет";
                    break;
                case "заправка":
                    searchPlace = "заправка";
                    searchType = "gas_station";
                    break;
                case "школа":
                    searchPlace = "школа";
                    searchType = "school";
                    break;
                case "метро":
                    searchPlace = "метро";
                    searchType = "subway_station";
                    break;
                case "кафе":
                    searchPlace = "кафе";
                    // searchType = "cafe";
                    break;
                case "банкомат":
                    searchPlace = "банкомат";
                    searchType = "atm";
                    break;
                case "супермаркет":
                    searchPlace = "супермаркет";
                    break;
                case "хочу":
                    chooseSearchingPlace(message, "Выбери что-нибудь поблизости:", message.getLocation(), searchType);
                    break;
                case "не хочу":
                    sendMsg(message, "Всего доброго!");
                    break;
                case "обновить координаты":
                    sendLoc(message, "Отправь, пожалуйста, свои координаты:");
                    break;
                default:
                    sendMsg(message, String.valueOf(update.getUpdateId()));
                    break;
            }
        }

        if (message.hasText() && text.equals(searchPlace) && locations.get(message.getChatId()) != null) {
            try {
                searchPlace(message, searchType);
//                chooseSearchingPlace(message, "Выбери что-нибудь поблизости:", message.getLocation(), searchType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (message.hasLocation()) {
            locations.put(message.getChatId(), message.getLocation());
            forwardLoc(message);
            chooseSearchingPlace(message, "Выбери что-нибудь поблизости:", message.getLocation(), searchType);
        }
    }

    @Override
    public String getBotUsername() {
        return Config.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Config.BOT_TOKEN;
    }

    public String getGoogleToken() {
        return Config.GOOGLE_PLACES_API_TOKEN;
    }

    @SuppressWarnings("deprecation") // в новых версиях метод изменяется или убирается
    private void sendMsg(Message msg, String text) {
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);

        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();

        message.setChatId(msg.getChatId())
                .setText(text)
                .setReplyMarkup(replyKeyboardRemove);
        try { // чтобы не крашнулась
            sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void sendPht(Message msg) {
        SendPhoto photo = new SendPhoto();
        final String photoCaption = "Твой Господин.";
        final String photoUrl = "https://pp.userapi.com/c840431/v840431484/2a926/anY0ALawqH4.jpg";
        photo.setChatId(msg.getChatId()).setCaption(photoCaption).setPhoto(photoUrl);

        try {
            sendPhoto(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void sendLoc(Message msg, String text) {
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);

        KeyboardButton keyboardButton = new KeyboardButton();
        final String locationButtonText = "Отправить геопозицию";
        keyboardButton.setRequestLocation(true).setText(locationButtonText);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow buttons = new KeyboardRow();
        buttons.add(keyboardButton);

        keyboard.add(buttons);
        replyKeyboardMarkup.setKeyboard(keyboard).setResizeKeyboard(true).setOneTimeKeyboard(false);

        message.setChatId(msg.getChatId())
                .setText(text)
                .setReplyMarkup(replyKeyboardMarkup);

        try {
            sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void forwardLoc(Message msg) {
        final String target = "@db_locations";
        ForwardMessage message = new ForwardMessage();
        message.setFromChatId(msg.getChatId());
        message.setChatId(target);
        message.setMessageId(msg.getMessageId());

        try {
            forwardMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void chooseSearchingPlace(Message msg, String text, Location location, String searchType) {
        SendMessage sendMessage = new SendMessage();

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("туалет");
        firstRow.add("метро");

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("супермаркет");
        secondRow.add("кафе");

        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add("банкомат");
        thirdRow.add("заправка");


        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);

        replyKeyboardMarkup.setResizeKeyboard(true)
                .setOneTimeKeyboard(false)
                .setSelective(true)
                .setKeyboard(keyboard);

        sendMessage.setChatId(msg.getChatId())
                .setChatId(msg.getChatId())
                .setText(text)
                .setReplyMarkup(replyKeyboardMarkup);

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void searchPlace(Message msg, String searchType) throws IOException {
        System.out.println(msg.getChatId());
        Location location = (Location) locations.get(msg.getChatId());
        System.out.println(location);
        final String baseUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json";
        final Map<String, String> params = Maps.newHashMap();
        params.put("key", getGoogleToken());
        params.put("query", msg.getText());
        params.put("location", String.format("%s,%s", location.getLatitude(), location.getLongitude()));
        params.put("language", "ru");
        if (searchType != null) {
            params.put("type", searchType);
        }
        params.put("radius", String.format("%d", 50));
        final String url = baseUrl + "?" + JsonReader.encodeParams(params);
        System.out.println(url);

        final JSONObject response = JsonReader.read(url);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(msg.getChatId());

        JSONObject jsonResult = response.getJSONArray("results").getJSONObject(0);

        final String name = jsonResult.getString("name");
        final String address = jsonResult.getString("formatted_address");

        final double rating;
        JSONObject resultInfo;
        boolean isOpenNow;
        String openNowString;

        if (jsonResult.has("opening_hours")) {
            if (jsonResult.has("rating")) {
                rating = jsonResult.getDouble("rating");
                resultInfo = jsonResult.getJSONObject("opening_hours");
                isOpenNow = resultInfo.getBoolean("open_now");
                openNowString = (isOpenNow) ? "Открыто сейчас" : "Закрыто сейчас";
                sendMessage.setText(String.format("%s%nРейтинг: %s%nАдрес: %s%n%s",
                        name,
                        rating,
                        address,
                        openNowString));
            } else {
                resultInfo = jsonResult.getJSONObject("opening_hours");
                isOpenNow = resultInfo.getBoolean("open_now");
                openNowString = (isOpenNow) ? "Открыто сейчас" : "Закрыто сейчас";
                sendMessage.setText(String.format("%s%nАдрес: %s%n%s",
                        name,
                        address,
                        openNowString));
            }
        } else if (!jsonResult.has("opening_hours")){
            if (jsonResult.has("rating")) {
                rating = jsonResult.getDouble("rating");
                sendMessage.setText(String.format("%s%nРейтинг: %s%nАдрес: %s",
                        name,
                        rating,
                        address));
            } else {
                sendMessage.setText(String.format("%s%nАдрес: %s",
                        name,
                        address));
            }
        }

        jsonResult = jsonResult.getJSONObject("geometry");
        jsonResult = jsonResult.getJSONObject("location");
        final double lng = jsonResult.getDouble("lng");// долгота
        final double lat = jsonResult.getDouble("lat");// широта

        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();

        SendLocation sendLocation = new SendLocation();
        sendLocation.setLatitude((float) lat);
        sendLocation.setLongitude((float) lng);
        sendLocation.setChatId(msg.getChatId());
        sendLocation.setReplyMarkup(replyKeyboardRemove);

        SendMessage resendingLoc = new SendMessage();
        String resendingText = "Для повторной отправки координат используй команду [/loc](/loc)";
        resendingLoc.setText(resendingText);
        resendingLoc.setChatId(msg.getChatId());
        resendingLoc.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkupOneMore = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("не хочу");
        firstRow.add("хочу");

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("обновить координаты");

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        replyKeyboardMarkupOneMore.setResizeKeyboard(true)
                .setOneTimeKeyboard(false)
                .setSelective(true)
                .setKeyboard(keyboard);

        SendMessage oneMore = new SendMessage();
        String oneMoreText = "Хотите посмотреть другие ближайшие места по заданым координатам?";
        oneMore.setChatId(msg.getChatId());
        oneMore.setText(oneMoreText);
        oneMore.setReplyMarkup(replyKeyboardMarkupOneMore);

        try {
            sendMessage(sendMessage);
            sendLocation(sendLocation);
            sendMessage(oneMore);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
