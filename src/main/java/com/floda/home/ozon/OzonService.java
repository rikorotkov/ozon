package com.floda.home.ozon;

import com.floda.home.telegram.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OzonService {

    private static final Logger log = LoggerFactory.getLogger(OzonService.class);
    private final TelegramBotService bot;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public OzonService(TelegramBotService bot) {
        this.bot = bot;
    }

    public void startCheckingOrders() {
        log.info("📡 Запуск мониторинга заказов...");
        scheduler.scheduleAtFixedRate(this::checkForOrders, 0, 30, TimeUnit.SECONDS);
    }

    private void checkForOrders() {
        try {
            log.info("Проверяю новые заказы ...");
            Set<String> newOrders = OzonClient.getNewOrders();
            for (String order : newOrders) {
                String message = "📦 Новый заказ! Номер: " + order;
                bot.sendOrderNotification(message);
                log.info("✅ Уведомление отправлено: {}", message);
            }
        } catch (Exception e) {
            log.error("🚨 Ошибка при проверке заказов: {}", e.getMessage());
        }
    }

    public void stopCheckingOrders() {
        scheduler.shutdown();
        log.info("🛑 Остановлен мониторинг заказов.");
    }

}
