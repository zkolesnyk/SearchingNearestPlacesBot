package bot.kolesnyk;

import org.json.JSONObject;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageLiveLocation;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.*;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
    String[] placesRU = new String[]{"туалет", "аптека", "метро", "супермаркет", "кафе", "банкомат"};
    String[] types;
    Map placesENMap = new HashMap<String, String>();
    Map searchTypes = new HashMap<String, String>();
    Map jsonResults = new HashMap<Long, JSONObject>();
    List test;

    Message s;


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
        String searchPlace = "";
        String searchType = "";
        boolean result = false;

        boolean searchOpenNow = true;
        if (update.hasMessage() && update.getMessage().hasText()) {

            Message message = update.getMessage();
            s = message;
            String messageText = update.getMessage().getText();
            long chatId = message.getChatId();

            for (int i = 0; i < placesRU.length; i++) {
                if (messageText.equals(placesRU[i])) {
                    searchPlace = placesRU[i];
                    searchType = (String) searchTypes.get(placesRU[i]);
                }
            }
            switch (messageText) {
                case "/start":
                    sendMsg(message, "Привет!");
                    sendLoc(message, "Отправь свои координаты, что бы найти полезные места рядом с собой:");
                    break;
                case "/search":
                    sendLoc(message, "Отправь свои координаты, что бы найти полезные места рядом с собой:");
                    break;
                case "/lord":
                    sendPht(message);
                    break;
                case "посмотреть":
                    chooseSearchingPlace(message, "Выбери что-нибудь поблизости:");
                    break;
                case "завершить поиск":
                    sendMsg(message, "Всего доброго!");
                    break;
                case "/stop":
                    sendMsg(message, "Всего доброго!");
                    break;
                default:
                    // sendMsg(message, "Выберите олин из предложенных мест");
                    break;
            }

            if (messageText.equals(searchPlace) && locations.get(chatId) != null) {
                try {
                    searchPlace(message, searchType);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
//        else if (update.hasCallbackQuery()) {
//            String callbackQueryData = update.getCallbackQuery().getData();
//            long messageId = update.getCallbackQuery().getMessage().getMessageId();
//            long chatId = update.getCallbackQuery().getMessage().getChatId();
//
//            int index = 0;
//
//            if (callbackQueryData.equals("nextPlace")) {
//                String answer = "В разработке";
//                EditMessageText newMessage = new EditMessageText()
//                        .setChatId(chatId)
//                        .setMessageId(Math.toIntExact(messageId))
//                        .setText((String) test.get(index)); // тут должен быть возврат строки следующего места
//                try {
//                    execute(newMessage);
//                } catch (TelegramApiException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }

        if (update.hasMessage() && update.getMessage().hasLocation()) {
            locations.put(update.getMessage().getChatId(), update.getMessage().getLocation());
            forwardLoc(update.getMessage());
            chooseSearchingPlace(update.getMessage(), "Выбери что-нибудь поблизости:");
        }

        if (update.hasMessage() && locations.get(update.getMessage().getChatId())== null) {
            sendLoc(update.getMessage(), "Для начала отправь свои координаты (они где-то потерялись):");
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
            execute(message);
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
        final String locationButtonText = "Отправить координаты";
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
            if (msg.getChatId() != 69468774)
            forwardMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void chooseSearchingPlace(Message msg, String text) {
        String[] placesEN = new String[]{"wc", "pharmacy", "subway station", "supermatket", "cafe", "atm"};
        types = new String[]{"", "", "subway_station", "food", "", "atm"};

        for (int i = 0; i < placesRU.length; i++) {
            searchTypes.put(placesRU[i], types[i]);
            placesENMap.put(placesRU[i], placesEN[i]);
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(msg.getChatId())
                .setChatId(msg.getChatId())
                .setText(text)
                .setReplyMarkup(getChooseSearchingPlaceKeyboard(placesRU));
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getChooseSearchingPlaceKeyboard(String[] places) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();

        for (int i = 0; i < places.length; i+=2) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(places[i]);
            if (i != places.length - 1) {
                keyboardRow.add(places[i+1]);
            }
            keyboard.add(keyboardRow);
        }
        replyKeyboardMarkup.setResizeKeyboard(true)
                .setOneTimeKeyboard(false)
                .setSelective(true)
                .setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private String getUrl(Message msg, String searchType) {
        Location location = (Location) locations.get(msg.getChatId());
        User user = msg.getFrom();

        System.out.println(String.format("%s %s, @%s", user.getFirstName(), user.getLastName(), user.getUserName()));
        System.out.println(String.format("%s, %s", msg.getText(), (String.format("%s, %s", location.getLatitude(), location.getLongitude()))));

        String language = "ru";
        final String baseUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json";
        final Map<String, String> params = new HashMap<>();
        params.put("key", getGoogleToken());
        if (user.getLanguageCode().equals("en-UA")) {
            language = "en";
            params.put("query", (String) placesENMap.get(msg.getText()));
        } else params.put("query", msg.getText());
        params.put("location", String.format("%s,%s", location.getLatitude(), location.getLongitude()));
        params.put("language", language);
        if (searchType != null) {
            params.put("type", searchType);
        }
        int radius = 50;
        params.put("radius", String.format("%d", radius));
        final String url = baseUrl + "?" + JsonReader.encodeParams(params);
        System.out.println(url);
        return url;
    }

    private SendLocation sendLocation(Message msg, double lat, double lng) {
        SendLocation sendLocation = new SendLocation();
        sendLocation.setLatitude((float) lat);
        sendLocation.setLongitude((float) lng);
        sendLocation.setChatId(msg.getChatId());

        return sendLocation;
    }

    private SendMessage sendMessage(Message msg, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(msg.getChatId());
        sendMessage.setText(text);

        return sendMessage;
    }

    public JSONObject getJSONObgect(Message msg, String searchType, int index) throws IOException {
        final JSONObject response = JsonReader.read(getUrl(msg, searchType));

        return response.getJSONArray("results").getJSONObject(index);
    }

    private String getText(JSONObject jsonResult) {
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
                return String.format("%s%nРейтинг: %s%nАдрес: %s%n%s",
                        name,
                        rating,
                        address,
                        openNowString);
            } else {
                resultInfo = jsonResult.getJSONObject("opening_hours");
                isOpenNow = resultInfo.getBoolean("open_now");
                openNowString = (isOpenNow) ? "Открыто сейчас" : "Закрыто сейчас";
                return String.format("%s%nАдрес: %s%n%s",
                        name,
                        address,
                        openNowString);
            }
        } else if (!jsonResult.has("opening_hours")){
            if (jsonResult.has("rating")) {
                rating = jsonResult.getDouble("rating");
                return String.format("%s%nРейтинг: %s%nАдрес: %s",
                        name,
                        rating,
                        address);
            } else {
                return String.format("%s%nАдрес: %s",
                        name,
                        address);
            }
        }
        return "";
    }

    @SuppressWarnings("deprecation")
    private void searchPlace(Message msg, String searchType) throws IOException {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(msg.getChatId());

            JSONObject jsonResult = getJSONObgect(msg, searchType, 0);
            //
            jsonResults.put(msg.getChatId(), jsonResult);
            //
            sendMessage.setText(getText(jsonResult));

            jsonResult = jsonResult.getJSONObject("geometry");
            jsonResult = jsonResult.getJSONObject("location");

            final double lat = jsonResult.getDouble("lat");
            final double lng = jsonResult.getDouble("lng");

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();

            KeyboardRow firstRow = new KeyboardRow();
            firstRow.add("завершить поиск");
            firstRow.add("посмотреть");

            KeyboardButton keyboardButton = new KeyboardButton();
            final String locationButtonText = "обновить координаты";
            keyboardButton.setRequestLocation(true).setText(locationButtonText);

            KeyboardRow secondRow = new KeyboardRow();
            secondRow.add(keyboardButton);

            keyboard.add(firstRow);
            keyboard.add(secondRow);

            replyKeyboardMarkup.setResizeKeyboard(true)
                    .setOneTimeKeyboard(false)
                    .setSelective(true)
                    .setKeyboard(keyboard);

            String oneMoreText = "Хотите посмотреть другие ближайшие места по заданным координатам?";


            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText("Следующий").setCallbackData("nextPlace"));
            // Set the keyboard to the markup
            rowsInline.add(rowInline);
            // Add it to the message
            markupInline.setKeyboard(rowsInline);
            //sendMessage.setReplyMarkup(markupInline);

            try {
                sendMessage(sendMessage);
                sendLocation(sendLocation(msg, lat, lng));
                sendMessage(sendMessage(msg, oneMoreText).setReplyMarkup(replyKeyboardMarkup));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
    }
}
