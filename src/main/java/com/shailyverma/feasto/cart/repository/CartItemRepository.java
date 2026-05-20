package com.shailyverma.feasto.cart.repository;

import com.shailyverma.feasto.cart.entity.Cart;
import com.shailyverma.feasto.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
