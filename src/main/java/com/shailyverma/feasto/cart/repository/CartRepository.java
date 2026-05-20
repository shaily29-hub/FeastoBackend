package com.shailyverma.feasto.cart.repository;

import com.shailyverma.feasto.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.expression.spel.ast.OpAnd;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

   Optional<Cart> findByUser_Id(Long userId);

}
