package com.shailyverma.feasto.ai;

import com.shailyverma.feasto.menu.entity.Menu;
import com.shailyverma.feasto.order.entity.Order;
import com.shailyverma.feasto.menu.repository.MenuRepository;
import com.shailyverma.feasto.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    private final OrderRepository orderRepository;
    private final UserPreferenceService userPreferenceService;

    private final MenuRepository menuRepository;
    List<Order> orders = new ArrayList<>();



    public AiService(MenuRepository menuRepository,
                     OrderRepository orderRepository,
                     UserPreferenceService userPreferenceService) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.userPreferenceService = userPreferenceService;
    }

    private final String URL =
            "https://openrouter.ai/api/v1/chat/completions";

    public String getAiReply(AiRequest request) {
        System.out.println("OPENROUTER KEY = " + apiKey);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setBearerAuth(apiKey);

        // ✅ FETCH MENU ITEMS

        List<Menu> menuItems = menuRepository.findAll();

        StringBuilder menuText = new StringBuilder();

        for (Menu item : menuItems) {

            menuText.append(item.getName())
                    .append(" - ")
                    .append(item.getDescription())
                    .append(", Price: ")
                    .append(item.getPrice())
                    .append("\n");
        }



        List<Order> orders = new ArrayList<>();

        System.out.println("========== AI DEBUG ==========");
        System.out.println("USER ID: " + request.getUserId());

        if (request.getUserId() != null) {

            orders =
                    orderRepository
                            .findByUserIdOrderByOrderDateDesc(
                                    request.getUserId()
                            );
        }

        System.out.println("TOTAL ORDERS: " + orders.size());
        // ✅ BUILD USER PREFERENCE SUMMARY

        String preferenceSummary =
                userPreferenceService
                        .buildPreferenceSummary(orders);

        // ✅ AI RECOMMENDATION PROMPT

        String prompt =
                "You are Feasto AI.\n" +
                        "You are a smart food assistant for a restaurant app.\n\n" +

                        "IMPORTANT RULES:\n" +
                        "- If user is greeting (hello, hi), respond naturally and briefly\n" +
                        "- If user asks for food recommendations, use order history\n" +
                        "- If user asks for menu, show menu items\n" +
                        "- If user asks for cheap food, filter low price items\n" +
                        "- If user asks for desserts, suggest desserts only\n" +
                        "- DO NOT always mention 'based on your order history'\n" +
                        "- Only mention order history when it is actually useful\n" +
                        "- Keep responses short and natural\n\n" +

                        "AVAILABLE MENU:\n" +
                        menuText + "\n\n" +

                        "USER ORDER INSIGHTS:\n" +
                        preferenceSummary + "\n\n" +

                        "USER MESSAGE:\n" +
                        request.getMessage();

        // ✅ CREATE MESSAGE

        Map<String, String> message = new HashMap<>();

        message.put("role", "user");

        message.put("content", prompt);

        // ✅ REQUEST BODY

        Map<String, Object> body = new HashMap<>();

        body.put(
                "model",
                "openai/gpt-3.5-turbo"
        );

        body.put(
                "messages",
                List.of(message)
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            URL,
                            HttpMethod.POST,
                            entity,
                            Map.class
                    );

            List choices =
                    (List) response.getBody().get("choices");

            Map firstChoice =
                    (Map) choices.get(0);

            Map messageMap =
                    (Map) firstChoice.get("message");

            return messageMap.get("content").toString();

        } catch (Exception e) {

            e.printStackTrace();

            return "AI recommendation service unavailable";
        }
    }
    }

