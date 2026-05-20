package com.shailyverma.feasto.order.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.category.entity.Category;
import com.shailyverma.feasto.enums.OrderStatus;
import com.shailyverma.feasto.enums.PaymentStatus;
import com.shailyverma.feasto.menu.entity.Menu;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "order_items")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ToString.Exclude
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name="order_id")
    private Order order;


    @ManyToOne
    @JoinColumn(name="menu_id")
    private Menu menu;


    private int quantity;

    private BigDecimal pricePerUnit;

    private BigDecimal subtotal;


}
