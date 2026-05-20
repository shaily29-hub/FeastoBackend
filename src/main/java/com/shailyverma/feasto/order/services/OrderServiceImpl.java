package com.shailyverma.feasto.order.services;

import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.email_notification.dtos.NotificationDTO;
import com.shailyverma.feasto.enums.PaymentStatus;
import com.shailyverma.feasto.menu.dtos.MenuDTO;
import com.shailyverma.feasto.order.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import lombok.RequiredArgsConstructor;
import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.auth_users.dtos.UserDTO;

import com.shailyverma.feasto.cart.entity.Cart;
import com.shailyverma.feasto.cart.entity.CartItem;

import com.shailyverma.feasto.order.entity.Order;
import com.shailyverma.feasto.order.entity.OrderItem;
import com.shailyverma.feasto.order.dtos.OrderDTO;
import com.shailyverma.feasto.order.dtos.OrderItemDTO;

import com.shailyverma.feasto.menu.dtos.MenuDTO;
import com.shailyverma.feasto.enums.OrderStatus;
import com.shailyverma.feasto.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.shailyverma.feasto.auth_users.services.UserService;
import com.shailyverma.feasto.cart.entity.Cart;
import com.shailyverma.feasto.cart.entity.CartItem;
import com.shailyverma.feasto.cart.repository.CartRepository;
import com.shailyverma.feasto.cart.services.CartService;
import com.shailyverma.feasto.email_notification.services.NotificationService;
import com.shailyverma.feasto.enums.OrderStatus;
import com.shailyverma.feasto.exceptions.BadRequestException;
import com.shailyverma.feasto.exceptions.NotFoundException;
import com.shailyverma.feasto.order.dtos.OrderDTO;
import com.shailyverma.feasto.order.dtos.OrderItemDTO;
import com.shailyverma.feasto.order.entity.OrderItem;
import com.shailyverma.feasto.order.repository.OrderItemRepository;
import com.shailyverma.feasto.order.repository.OrderRepository;
import com.shailyverma.feasto.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final CartService cartService;
    private final CartRepository cartRepository;

    @Value("${base.payment.link}")
    private String basePaymentLink;


    @Override
    @Transactional
    public Response<?> placeOrderFromCart() {

        log.info("Inside placeOrderFromCart()");

        User customer = userService.getCurrentLoggedInUser();

        String deliveryAddress = customer.getAddress();
        if (deliveryAddress == null) {
            throw new NotFoundException("Delivery Address Not present for the user");
        }

        Cart cart = cartRepository.findByUser_Id(customer.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for the user"));

        List<CartItem> cartItems = cart.getCartItems();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // STEP 1: build order items
        for (CartItem cartItem : cartItems) {

            OrderItem orderItem = OrderItem.builder()
                    .menu(cartItem.getMenu())
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(cartItem.getPricePerUnit())
                    .subtotal(cartItem.getSubtotal())
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        // STEP 2: create order
        Order order = Order.builder()
                .user(customer)
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.INITIALIZED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // STEP 3: IMPORTANT → set both sides BEFORE save
        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        // STEP 4: SAVE ONLY ORDER (cascade handles order_items)
        Order savedOrder = orderRepository.save(order);

        // STEP 5: clear cart
        cartService.clearShoppingCart();

        // STEP 6: send email (safe DTO usage)
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        sendOrderConfirmationEmail(customer, orderDTO);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Your address has been received! we've sent a secure payment linkto your email.proceed for payment")
                .build();
    }



    @Override
    public Response<OrderDTO> getOrderById(Long id) {
        log.info("Inside getOrderById()");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));

        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order retrieved successfully")
                .data(orderDTO)
                .build();
    }


    @Override
    public Response<Page<OrderDTO>> getAllOrder(OrderStatus orderStatus, int page, int size) {
        log.info("Inside getAllOrder()");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Order> orderPage;

        if (orderStatus != null) {
            orderPage = orderRepository.findByOrderStatus(orderStatus, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }

        Page<OrderDTO> orderDTOsPage = orderPage.map(order -> {
            OrderDTO dto = modelMapper.map(order, OrderDTO.class);
            dto.getOrderItems().forEach(orderItemDTO -> orderItemDTO.getMenu().setReviews(null));
            return dto;
        });

        return Response.<Page<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Orders retrieved successfully")
                .data(orderDTOsPage)
                .build();
    }


    @Override
    public Response<List<OrderDTO>> getOrdersOfUser() {
        log.info("Inside getOrdersOfUser()");
        // Inside the relevant method (e.g., getOrdersForUser)
        User customer = userService.getCurrentLoggedInUser();
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(customer);

        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();

        orderDTOs.forEach(orderItem -> {
            orderItem.setUser(null);
            orderItem.getOrderItems().forEach(item -> item.getMenu().setReviews(null));
        });

        return Response.<List<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Orders for user retrieved successfully")
                .data(orderDTOs)
                .build();
    }

    @Override
    public Response<OrderItemDTO> getOrderItembyId(Long orderItemId) {
        log.info("Inside getOrderItemById()");

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order Item Not Found"));

        OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);

        // Manual mapping for menu if not automatically handled by ModelMapper
        orderItemDTO.setMenu(modelMapper.map(orderItem.getMenu(), MenuDTO.class));

        return Response.<OrderItemDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order Item retrieved successfully")
                .data(orderItemDTO)
                .build();
    }

    @Override
    public Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO) {
        log.info("Inside updateOrderStatus()");

        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new NotFoundException("Order not found: "));

        OrderStatus orderStatus = orderDTO.getOrderStatus();
        order.setOrderStatus(orderStatus);

        orderRepository.save(order);

        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order status updated successfully")
                .build();

    }

    @Override
    public Response<Long> countUniqueCustomers() {
        log.info("Inside countUniqueCustomers()");

        long uniqueCustomerCount = orderRepository.countDistinctUsers();

        return Response.<Long>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Unique customer count retrieved successfully")
                .data(uniqueCustomerCount)
                .build();
    }


    private void sendOrderConfirmationEmail(User customer, OrderDTO orderDTO) {

        log.info("EMAIL METHOD CALLED");

        String subject = "Your Order Confirmation - Order #" + orderDTO.getId();

        // Use org.thymeleaf.context.Context, NOT javax.naming.Context
        Context context = new Context(Locale.getDefault());

        context.setVariable("customerName", customer.getName());
        context.setVariable("orderId", String.valueOf(orderDTO.getId()));
        context.setVariable("orderDate", orderDTO.getOrderDate().toString());
        context.setVariable("totalAmount", orderDTO.getTotalAmount().toString());

        // Format delivery address
        String deliveryAddress = orderDTO.getUser().getAddress();
        context.setVariable("deliveryAddress", deliveryAddress);

        context.setVariable("currentYear", java.time.Year.now());

        // Build the order items HTML using StringBuilder+

        StringBuilder orderItemsHtml = new StringBuilder();

        for (OrderItemDTO item : orderDTO.getOrderItems()) {
            orderItemsHtml.append("<div class=\"order-item\">")
                    .append("<p>").append(item.getMenu().getName()).append(" x").append(item.getQuantity()).append("</p>")
                    .append("<p> $").append(item.getSubtotal()).append("</p>")
                    .append("</div>");
        }

        context.setVariable("orderItemsHtml", orderItemsHtml.toString());
        context.setVariable("totalItems", orderDTO.getOrderItems().size());

        String paymentLink = basePaymentLink + orderDTO.getId() + "&amount=" + orderDTO.getTotalAmount();
        context.setVariable("paymentLink", paymentLink);

        // Process the Thymeleaf template to generate the HTML email body
        String emailBody = templateEngine.process("order-confirmation", context);

        notificationService.sendEmail(NotificationDTO.builder()
                .recipient(customer.getEmail())
                .subject(subject)
                .body(emailBody)
                .isHtml(true)
                .build());
    }
}

