package com.shailyverma.feasto.payment.dtos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.shailyverma.feasto.auth_users.dtos.UserDTO;
import com.shailyverma.feasto.enums.PaymentGateway;
import com.shailyverma.feasto.enums.PaymentStatus;
import com.shailyverma.feasto.order.dtos.OrderDTO;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class PaymentDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;


    private BigDecimal amount;


    private PaymentStatus paymentStatus;

    private String transactionId;


    private PaymentGateway paymentGateway;


    private String failureReason;


    private boolean success;

    private LocalDateTime paymentdate;

    private OrderDTO order;

    private UserDTO user;
}
