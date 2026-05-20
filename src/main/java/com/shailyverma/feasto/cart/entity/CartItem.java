package com.shailyverma.feasto.cart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shailyverma.feasto.menu.entity.Menu;
import jakarta.persistence.*;

import java.math.BigDecimal;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Entity   // ✅ ADD THIS
@Table(name = "cart_items") // (optional but recommended)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    private int quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal subtotal;
}
