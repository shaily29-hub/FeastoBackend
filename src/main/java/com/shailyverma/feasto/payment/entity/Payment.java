package com.shailyverma.feasto.payment.entity;
import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.enums.PaymentGateway;
import com.shailyverma.feasto.enums.PaymentStatus;
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
@Table(name = "payment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;


    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentGateway paymentGateway;


    private String failureReason;


    private LocalDateTime paymentdate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



}
