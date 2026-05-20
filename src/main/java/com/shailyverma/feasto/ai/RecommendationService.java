package com.shailyverma.feasto.ai;

import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.menu.entity.Menu;
import com.shailyverma.feasto.menu.repository.MenuRepository;
import com.shailyverma.feasto.order.entity.Order;
import com.shailyverma.feasto.order.entity.OrderItem;
import com.shailyverma.feasto.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final OrderRepository orderRepository;

    private final MenuRepository menuRepository;

    public List<Menu> getRecommendations(User user) {

        List<Order> orders =
                orderRepository
                        .findByUserOrderByOrderDateDesc(user);

        // CATEGORY FREQUENCY

        Map<String, Integer> categoryFrequency =
                new HashMap<>();

        for (Order order : orders) {

            for (OrderItem item : order.getOrderItems()) {

                String categoryName =
                        item.getMenu()
                                .getCategory()
                                .getName();

                categoryFrequency.put(
                        categoryName,
                        categoryFrequency
                                .getOrDefault(
                                        categoryName,
                                        0
                                ) + item.getQuantity()
                );
            }
        }

        // FIND FAVORITE CATEGORY

        String favoriteCategory = null;

        int max = 0;

        for (Map.Entry<String, Integer> entry
                : categoryFrequency.entrySet()) {

            if (entry.getValue() > max) {

                max = entry.getValue();

                favoriteCategory = entry.getKey();
            }
        }

        // IF NO HISTORY

        if (favoriteCategory == null) {

            return menuRepository.findAll()
                    .stream()
                    .limit(5)
                    .toList();
        }

        // RECOMMEND SAME CATEGORY ITEMS
        final String finalFavoriteCategory =
                favoriteCategory;

        return menuRepository.findAll()
                .stream()
                .filter(menu ->

                        menu.getCategory()
                                .getName()
                                .equalsIgnoreCase(
                                        finalFavoriteCategory
                                )
                )
                .limit(5)
                .toList();
    }
}