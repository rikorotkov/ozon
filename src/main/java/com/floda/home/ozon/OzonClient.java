package com.floda.home.ozon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.floda.home.model.Order;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OzonClient {

    private static final Dotenv env = Dotenv.load();
    private static final String OZON_API = env.get("OZON_API");
    private static final String OZON_CLIENT_ID = env.get("OZON_CLIENT_ID");
    private static final String API_URL = "https://api-seller.ozon.ru/v3/posting/fbs/unfulfilled/list";
    private static final Logger log = LoggerFactory.getLogger(OzonClient.class);
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> processedOrders = new HashSet<>();

    public static Set<Order> getNewOrders() throws Exception {
        Set<Order> newOrders = new HashSet<>();

        String requestBody = "{ \"dir\": \"ASC\", \"limit\": 100 }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL))
                .header("Client-Id", OZON_CLIENT_ID)
                .header("Api-Key", OZON_API)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        JsonNode orders = root.path("result").path("postings");

        Iterator<JsonNode> items = orders.elements();
        while (items.hasNext()) {
            JsonNode order = items.next();
            String orderId = order.get("posting_number").asText();
            if (!processedOrders.contains(orderId)) {
                processedOrders.add(orderId);
                String totalAmount = order.path("financial_data").path("products").get(0).path("price").asText();
                String productName = order.path("products").get(0).path("name").asText();
                String productId = order.path("products").get(0).path("offer_id").asText();
                String imageUrl = getImageUrlByProductId(productId);
                int quantity = order.path("products").get(0).path("quantity").asInt();

                newOrders.add(new Order(orderId, totalAmount, productName, productId, imageUrl, quantity));
                log.info("📦 Новый заказ: {}", orderId);
            }
        }

        return newOrders;
    }

    public static String getImageUrlByProductId(String productId) {
        try {
            // Создаем тело запроса
            String requestBody = String.format("{\"offer_id\": [\"%s\"]}", productId);

            // Отправляем POST-запрос к API Ozon
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Client-Id", OZON_CLIENT_ID)
                    .header("Api-Key", OZON_API)
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Преобразуем ответ в JsonNode
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            // Извлекаем массив items
            JsonNode items = root.path("items");

            // Проверяем, что items не пустой
            if (items.isArray() && items.size() > 0) {
                // Извлекаем первый элемент из массива items
                JsonNode item = items.get(0);

                // Извлекаем поле primary_image, которое является массивом
                JsonNode primaryImage = item.path("primary_image");

                // Проверяем, что primary_image не пустой
                if (primaryImage.isArray() && primaryImage.size() > 0) {
                    // Берем первый элемент из массива primary_image
                    return primaryImage.get(0).asText();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

}
