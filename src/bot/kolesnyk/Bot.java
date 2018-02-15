package bot.kolesnyk;

import com.google.common.collect.Maps;
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
import java.util.List;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {

    Location userLocation = new Location();

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
        if (message.hasText()) {
            switch (text) {
                case "/start":
                    sendMsg(message, "Привет. ");
                    sendMsg(message, "Поделись своим местоположением /loc");
                    break;
                case "/loc":
                    sendLoc(message, "Нажми на кнопку ниже, что бы отправить свои координаты");
                    break;
                case "/lord":
                    sendPht(message);
                    break;
                case "туалет":
                    searchPlace = "туалет";
                    break;
                case "заправка":
                    searchPlace = "заправка";
                    break;
                case "школа":
                    searchPlace = "школа";
                    searchType = "school";
                    break;
                case "метро":
                    searchPlace = "метро";
                    break;
                case "рестораны":
                    searchPlace = "метро";
                    break;
                default:
                    sendMsg(message, String.valueOf(update.getUpdateId()));
                    break;
            }
        }
        if (message.hasText() && text.equals(searchPlace)) {
            try {
                searchPlace(message, userLocation, searchType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (message.hasLocation()) {
            userLocation = message.getLocation();
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
        firstRow.add("заправка");

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("школа");
        secondRow.add("метро");

        keyboard.add(firstRow);
        keyboard.add(secondRow);

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
//            searchPlace(msg, location, searchType);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void searchPlace(Message msg, Location location, String searchType) throws IOException {
        final String baseUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json";
        final Map<String, String> params = Maps.newHashMap();
        params.put("key", getGoogleToken());
        params.put("query", msg.getText());
        params.put("location", String.format("%s,%s", location.getLatitude(), location.getLongitude()));
        params.put("language", "ru");
        if (searchType != null) {
            params.put("type", searchType);
        }
        params.put("radius", String.format("%d", 100));
        final String url = baseUrl + "?" + JsonReader.encodeParams(params);
        System.out.println(url);

        final JSONObject response = JsonReader.read(url);
        JSONObject resultLocation = response.getJSONArray("results").getJSONObject(0);
        final String name = resultLocation.getString("name");
        final double rating = resultLocation.getDouble("rating");
        resultLocation = resultLocation.getJSONObject("geometry");
        resultLocation = resultLocation.getJSONObject("location");
        final double lng = resultLocation.getDouble("lng");// долгота
        final double lat = resultLocation.getDouble("lat");// широта

        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();

        SendLocation sendLocation = new SendLocation();
        sendLocation.setLatitude((float) lat);
        sendLocation.setLongitude((float) lng);
        sendLocation.setChatId(msg.getChatId());
        sendLocation.setReplyMarkup(replyKeyboardRemove);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(msg.getChatId());
        sendMessage.setText(String.format("%s%nРейтинг: %s", name, rating));

        try {
            sendMessage(sendMessage);
            sendLocation(sendLocation);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
