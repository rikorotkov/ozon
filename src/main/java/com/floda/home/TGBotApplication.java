package com.floda.home;

import com.floda.home.ozon.OzonService;
import com.floda.home.telegram.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TGBotApplication {

    private static final Logger log = LoggerFactory.getLogger(TGBotApplication.class);
    private static OzonService ozonService;  // Сохраняем в поле класса

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            TelegramBotService bot = new TelegramBotService();
            botsApi.registerBot(bot);

            log.info("🤖 Бот запущен!");

            // Создаем сервис и сохраняем в поле класса
            ozonService = new OzonService(bot);
            ozonService.startCheckingOrders();

        } catch (Exception e) {
            log.error("🚨 Ошибка запуска бота: {}", e.getMessage());
        }
    }

}
