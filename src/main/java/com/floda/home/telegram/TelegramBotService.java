package com.floda.home.telegram;

import com.floda.home.model.Order;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashSet;
import java.util.Set;

public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);
    private static final Dotenv env = Dotenv.load();
    private static final String BOT_TOKEN = env.get("BOT_TOKEN");
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
            } else if (text.equalsIgnoreCase("/test")) {
                Order test = new Order("2315123123", "506", "2","Свечи ароматические", "https://ir.ozone.ru/s3/multimedia-1-5/wc1000/7301915825.jpg", 1);
                String message = "📦 Новый заказ: \n" +
                        "🆔 Номер: " + test.getOrderId() + "\n" +
                        "💰 Сумма: " + test.getTotalAmount() + " руб.\n" +
                        "💼 Товар: " + test.getProductName() + "\n" +
                        "🔢 Количество: " + test.getQuantity();

                if (users.contains(chatId)) {
                    sendOrderNotification(message, test.getProductImageUrl());
                }
            }
        }
    }

    public void sendOrderNotification(String orderData, String imageUrl) {
        for (String chatId : users) {
            if (imageUrl != null) {
                sendImageMessage(chatId, orderData, imageUrl);
            } else {
                sendMessage(chatId, orderData);
            }
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

    private void sendImageMessage(String chatId, String text, String imageUrl) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(text);
        sendPhoto.setPhoto(new InputFile(imageUrl));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
