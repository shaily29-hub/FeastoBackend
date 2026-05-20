package com.shailyverma.feasto.review.entity;

import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.enums.PaymentGateway;
import com.shailyverma.feasto.enums.PaymentStatus;
import com.shailyverma.feasto.menu.entity.Menu;
import com.shailyverma.feasto.order.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "reviews")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @ManyToOne
 @JoinColumn(name = "user_id",nullable=false)
 private User user;


 private Integer rating;

 @Column(columnDefinition = "TEXT")
 private String comment;


 private LocalDateTime createdAt;

 @Column(name = "order_id")
 private Long orderId;


 @ManyToOne
 @JoinColumn(name = "menu_id")
 private Menu menu;

}