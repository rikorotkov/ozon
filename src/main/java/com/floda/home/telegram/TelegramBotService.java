package com.floda.home.telegram;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashSet;
import java.util.Set;

public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);
    private static final Dotenv env = Dotenv.load();
    private static final String BOT_TOKEN = env.get("BOT_TOKEN");
    private static final String CHAT_ID = env.get("CHAT_ID");
    private static final Set<String> users = new HashSet<>();


    public TelegramBotService() {
        super(BOT_TOKEN);
    }

    @Override
    public String getBotUsername() {
        return "Forma Casa Bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            if (text.equalsIgnoreCase("/start")) {
                if (users.contains(chatId)) {
                    sendMessage(chatId, "⚠\uFE0F Вы уже подписаны на уведомления о заказах!");
                    log.info(chatId + " уже подписан на уведомления!");
                    return;
                }
                users.add(chatId);
                sendMessage(chatId, "✅ Вы подписаны на уведомления о заказах!");
                log.info(chatId + " подписался на уведомления.");
            } else if (text.equalsIgnoreCase("/stop")) {
                if (users.contains(chatId)) {
                    users.remove(chatId);
                    sendMessage(chatId, "❌ Вы отключили уведомления о заказах.");
                    log.info(chatId + " отключил уведомления.");
                }
            }
        }
    }

    public void sendOrderNotification(String orderData) {
        for (String chatId : users) {
            sendMessage(chatId, "📦 Новый заказ: " + orderData);
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
