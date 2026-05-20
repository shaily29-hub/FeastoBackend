package com.shailyverma.feasto.payment.services;

import com.shailyverma.feasto.email_notification.dtos.NotificationDTO;
import com.shailyverma.feasto.email_notification.services.NotificationService;
import com.shailyverma.feasto.enums.OrderStatus;
import com.shailyverma.feasto.enums.PaymentGateway;
import com.shailyverma.feasto.enums.PaymentStatus;
import com.shailyverma.feasto.exceptions.BadRequestException;
import com.shailyverma.feasto.exceptions.NotFoundException;
import com.shailyverma.feasto.order.dtos.OrderItemDTO;
import com.shailyverma.feasto.order.entity.Order;
import com.shailyverma.feasto.order.repository.OrderRepository;
import com.shailyverma.feasto.payment.dtos.PaymentDTO;
import com.shailyverma.feasto.payment.entity.Payment;
import com.shailyverma.feasto.payment.repository.PaymentRepository;
import com.shailyverma.feasto.response.Response;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;
    private final ModelMapper modelMapper;

    @Value("${stripe.api.secret.key}")
    private String secretKey;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;
    @Override
    public Response<?> initializePayment(PaymentDTO paymentRequest) {

        log.info("Inside initializePayment()");

        Stripe.apiKey = secretKey;

        Long orderId = paymentRequest.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment Already Made For This Order");
        }

        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid payment amount");
        }

        if (order.getTotalAmount().compareTo(paymentRequest.getAmount()) != 0) {
            throw new BadRequestException("Payment Amount Does Not Match Order Total");
        }

        try {

            log.info("Amount received from frontend: {}", paymentRequest.getAmount());

            long amountInPaise = paymentRequest.getAmount()
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            log.info("Amount in paise: {}", amountInPaise);

            log.info("Creating Stripe PaymentIntent...");

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInPaise)
                    .setCurrency("inr")
                    .putMetadata("orderId", String.valueOf(orderId))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            log.info("Stripe PaymentIntent ID: {}", intent.getId());
            log.info("Stripe ClientSecret generated successfully");

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("success")
                    .data(intent.getClientSecret())
                    .build();

        } catch (com.stripe.exception.StripeException se) {

            log.error("Stripe API Error Message: {}", se.getMessage(), se);
            log.error("Stripe Status Code: {}", se.getStatusCode());
            log.error("Stripe Request ID: {}", se.getRequestId());

            throw new RuntimeException("Stripe Payment Failed: " + se.getMessage());

        } catch (Exception e) {

            log.error("General Payment Error: {}", e.getMessage(), e);

            throw new RuntimeException("Payment Failed: " + e.getMessage());
        }
    }


    @Transactional
    public void updatePaymentForOrder(PaymentDTO paymentDTO) {

        log.info("INSIDE updatePaymentForOrder");

        try {

            Order order = orderRepository.findById(paymentDTO.getOrderId())
                    .orElseThrow(() -> new NotFoundException("Order Not Found"));

            if (order.getUser() == null) {
                throw new RuntimeException("Order user not found");
            }

            boolean isSuccess = paymentDTO.isSuccess();

            Payment payment = new Payment();
            payment.setPaymentGateway(PaymentGateway.STRIPE);
            payment.setAmount(paymentDTO.getAmount());
            payment.setTransactionId(paymentDTO.getTransactionId());
            payment.setPaymentdate(LocalDateTime.now());
            payment.setOrder(order);

            payment.setPaymentStatus(
                    isSuccess ? PaymentStatus.COMPLETED : PaymentStatus.FAILED
            );

            // UPDATE ORDER
            if (isSuccess) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setOrderStatus(OrderStatus.CONFIRMED);
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setOrderStatus(OrderStatus.CANCELLED);
            }

            // SAVE FIRST
            paymentRepository.save(payment);
            orderRepository.save(order);

            // EMAIL AFTER SAVE
            Context context = new Context(Locale.getDefault());
            context.setVariable("customerName", order.getUser().getName());
            context.setVariable("orderId", order.getId());
            context.setVariable("currentYear", Year.now().getValue());
            context.setVariable("amount", paymentDTO.getAmount());

            if (isSuccess) {

                String emailBody = templateEngine.process("payment-successful", context);

                notificationService.sendEmail(NotificationDTO.builder()
                        .recipient(order.getUser().getEmail())
                        .subject("Payment Successful - Order #" + order.getId())
                        .body(emailBody)
                        .isHtml(true)
                        .build());

            } else {

                context.setVariable("failureReason", paymentDTO.getFailureReason());

                String emailBody = templateEngine.process("payment-failed", context);

                notificationService.sendEmail(NotificationDTO.builder()
                        .recipient(order.getUser().getEmail())
                        .subject("Payment Failed - Order #" + order.getId())
                        .body(emailBody)
                        .isHtml(true)
                        .build());
            }

            log.info("PAYMENT UPDATED SUCCESSFULLY");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response<List<PaymentDTO>> getAllPayments() {
        log.info("Inside getAllPayments()");

        List<Payment> paymentList = paymentRepository.findAll(
                Sort.by(Sort.Direction.DESC, "id")
        );

        List<PaymentDTO> paymentDTOS = modelMapper.map(
                paymentList,
                new TypeToken<List<PaymentDTO>>() {}.getType()
        );

        paymentDTOS.forEach((PaymentDTO item) -> {
            item.setOrder(null);
            item.setUser(null);
        });

        return Response.<List<PaymentDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("payment retrieved successfully")
                .data(paymentDTOS)
                .build();
    }

    @Override
    public Response<PaymentDTO> getPaymentById(Long paymentId) {
        log.info("Inside getPaymentById()");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        PaymentDTO paymentDTOS = modelMapper.map(payment, PaymentDTO.class);

        paymentDTOS.getUser().setRoles(null);
        paymentDTOS.getOrder().setUser(null);

        paymentDTOS.getOrder().getOrderItems().forEach((OrderItemDTO item) -> {
            item.getMenu().setReviews(null);
        });

        return Response.<PaymentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("payment retrieved successfully by id")
                .data(paymentDTOS)
                .build();
    }
}
