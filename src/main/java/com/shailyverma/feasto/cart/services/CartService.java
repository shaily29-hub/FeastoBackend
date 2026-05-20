package com.shailyverma.feasto.cart.services;

import com.shailyverma.feasto.cart.dtos.CartDTO;
import com.shailyverma.feasto.response.Response;

public interface CartService {
    Response<?> addItemToCart(CartDTO cartDTO);
    Response<?> incrementItem(Long menuId);
    Response<?> decrementItem(Long menuId);
    Response<?> removeItem(Long cartItemId);
    Response<CartDTO> getShoppingCart();
    Response<?> clearShoppingCart();
}
