package com.shailyverma.feasto.cart.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.shailyverma.feasto.menu.dtos.MenuDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItemDTO {

    private Long id;
    private Long menuId;
    private int quantity;
    private BigDecimal totalAmount;
    private MenuDTO menu;
    





}
