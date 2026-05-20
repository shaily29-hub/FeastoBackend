package com.shailyverma.feasto.cart.controller;

import com.shailyverma.feasto.cart.dtos.CartDTO;
import com.shailyverma.feasto.cart.services.CartService;
import com.shailyverma.feasto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart") // Assuming a base path, though not visible in snippet
@RequiredArgsConstructor
public class CartController {


        private final CartService cartService;

        @PostMapping("/items")
        public ResponseEntity<Response<?>> addItemToCart(@RequestBody CartDTO cartDTO) {
            return ResponseEntity.ok(cartService.addItemToCart(cartDTO));
        }

        @PutMapping("/items/increment/{menuId}")
        public ResponseEntity<Response<?>> incrementItem(@PathVariable Long menuId) {
            return ResponseEntity.ok(cartService.incrementItem(menuId));
        }

        @PutMapping("/items/decrement/{menuId}")
        public ResponseEntity<Response<?>> decrementItem(@PathVariable Long menuId) {
            return ResponseEntity.ok(cartService.decrementItem(menuId));
        }

        @DeleteMapping("/items/{cartItemId}")
        public ResponseEntity<Response<?>> removeItem(@PathVariable Long cartItemId) {
            return ResponseEntity.ok(cartService.removeItem(cartItemId));
        }

    @GetMapping
    public ResponseEntity<Response<CartDTO>> getShoppingCart() {
        return ResponseEntity.ok(cartService.getShoppingCart());
    }

    @DeleteMapping
    public ResponseEntity<Response<?>> clearShoppingCart() {
        return ResponseEntity.ok(cartService.clearShoppingCart());
    }
}
