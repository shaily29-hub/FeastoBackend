package com.shailyverma.feasto.ai;

import com.shailyverma.feasto.order.entity.Order;
import com.shailyverma.feasto.order.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserPreferenceService {

    public String buildPreferenceSummary(List<Order> orders) {

        if (orders == null || orders.isEmpty()) {
            return "No previous order history.";
        }

        Map<String, Integer> itemFrequency = new HashMap<>();

        Map<String, Integer> categoryFrequency =
                new HashMap<>();

        for (Order order : orders) {

            for (OrderItem item : order.getOrderItems()) {

                // FOOD ITEM COUNT

                String itemName =
                        item.getMenu().getName();

                itemFrequency.put(
                        itemName,
                        itemFrequency.getOrDefault(
                                itemName,
                                0
                        ) + item.getQuantity()
                );

                // CATEGORY COUNT

                String categoryName =
                        item.getMenu()
                                .getCategory()
                                .getName();

                categoryFrequency.put(
                        categoryName,
                        categoryFrequency.getOrDefault(
                                categoryName,
                                0
                        ) + item.getQuantity()
                );
            }
        }

        StringBuilder summary =
                new StringBuilder();

        summary.append("Favorite Foods:\n");

        itemFrequency.forEach((item, count) -> {

            summary.append("- ")
                    .append(item)
                    .append(": ")
                    .append(count)
                    .append(" orders\n");
        });

        summary.append("\nFavorite Categories:\n");

        categoryFrequency.forEach((category, count) -> {

            summary.append("- ")
                    .append(category)
                    .append(": ")
                    .append(count)
                    .append(" orders\n");
        });

        return summary.toString();
    }
}