package com.shailyverma.feasto.order.repository;

import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.enums.OrderStatus;
import com.shailyverma.feasto.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    Page<Order> findByOrderStatus(
            OrderStatus status,
            Pageable pageable
    );

    List<Order> findByUserOrderByOrderDateDesc(
            User user
    );

    // ✅ NEW METHOD

    List<Order> findByUserIdOrderByOrderDateDesc(
            Long userId
    );

    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o")
    long countDistinctUsers();
}