package com.floda.home.ozon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class OzonClient {

    private static final Dotenv env = Dotenv.load();
    private static final String OZON_API = env.get("OZON_API");
    private static final String OZON_CLIENT_ID = env.get("OZON_CLIENT_ID");
    private static final String API_URL = "https://api-seller.ozon.ru/v3/posting/fbs/list";
    private static final Logger log = LoggerFactory.getLogger(OzonClient.class);
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<String> processedOrders = new HashSet<>(); // Храним уже обработанные заказы

    public static Set<String> getNewOrders() throws Exception {
        Set<String> newOrders = new HashSet<>();

        String requestBody = "{ \"dir\": \"ASC\", \"filter\": { \"since\": \"2025-02-27T00:00:00Z\" }, \"limit\": 10 }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL))
                .header("Client-Id", OZON_CLIENT_ID)
                .header("Api-Key", OZON_API)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        JsonNode orders = root.path("result");

        Iterator<JsonNode> items = orders.elements();
        while (items.hasNext()) {
            JsonNode order = items.next();
            String orderId = order.get("posting_number").asText();

            if (!processedOrders.contains(orderId)) {
                newOrders.add(orderId);
                processedOrders.add(orderId);
                log.info("📦 Новый заказ: {}", orderId);
            }
        }

        return newOrders;
    }

}
