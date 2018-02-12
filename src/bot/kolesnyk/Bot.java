package bot.kolesnyk;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiValidationException;


public class Bot extends TelegramLongPollingBot {

    public static void main(String[] args) {
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
                    sendMsg(message, "Привет. Это простой бот с одной командой /start");
                    break;
                case "/loc":
                    sendLoc(message, "Нажмите на кнопку, что бы отправить свои координаты");
                    break;
                default:
                    sendMsg(message, String.valueOf(update.getUpdateId()));
                    break;
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "kolesnyk_bot"; // озвращаем юзера
    }

    @Override
    public String getBotToken() {
        return "517084638:AAGMSgCFPRw94YNLSlR7IerekN4lC2EcgdQ";
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
    private void sendLoc(Message msg, String text) {
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);

        KeyboardButton reply = new KeyboardButton();
        reply.setRequestLocation(true);
        reply.setText("отправить геопозицию");

        message.setChatId(msg.getChatId());
        message.setText(text);

        try { // чтобы не крашнулась
            sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
