package bot.kolesnyk;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


public class Bot extends TelegramLongPollingBot {

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
        if (message.hasText()) {
            switch (text) {
                case "/start":
                    sendMsg(message, "Привет. ");
                    sendMsg(message, "Ты можешь поделиться со мной своим местоположением, введя команду /loc и жмякнув на кнопку \"отправить геопозицию\". Правда, пока я не придумал, как его можно использовать.");
                    break;
                case "/loc":
                    sendLoc(message, "Нажмите на кнопку, что бы отправить свои координаты");
                    break;
                case "/father":
                    sendPht(message);
                    break;
                default:
                    sendMsg(message, String.valueOf(update.getUpdateId()));
                    break;
            }
        }
        if (message.hasLocation()) forwardLoc(message, "одну минуточку");
    }

    @Override
    public String getBotUsername() {
        return Config.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Config.BOT_TOKEN;
    }

    @SuppressWarnings("deprecation") // в новых версиях метод изменяется или убирается
    private void sendMsg(Message msg, String text) {
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);

        message.setChatId(msg.getChatId());
        message.setText(text);
        try { // чтобы не крашнулась
            sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void sendPht(Message msg) {
        SendPhoto photo = new SendPhoto();
        final String photoCaption = "Твой господин.";
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
        final String locationButtonText = "отправить геопозицию";
        keyboardButton.setRequestLocation(true).setText(locationButtonText);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow buttons = new KeyboardRow();
        buttons.add(keyboardButton);

        keyboard.add(buttons);
        replyKeyboardMarkup.setKeyboard(keyboard).setResizeKeyboard(true).setOneTimeKeyboard(false);

        message.setChatId(msg.getChatId()).setText(text).setReplyMarkup(replyKeyboardMarkup);

        try {
            sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void forwardLoc(Message msg, String text) {
        final String target = "69468774";
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
}
