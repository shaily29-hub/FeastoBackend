package com.shailyverma.feasto.order.services;

import com.shailyverma.feasto.enums.OrderStatus;
import com.shailyverma.feasto.order.dtos.OrderDTO;
import com.shailyverma.feasto.order.dtos.OrderItemDTO;
import com.shailyverma.feasto.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Response<?> placeOrderFromCart();

    Response<OrderDTO> getOrderById(Long id);

    Response<Page<OrderDTO>> getAllOrder(OrderStatus orderStatus, int page, int size);
    Response<List<OrderDTO>> getOrdersOfUser();
    Response<OrderItemDTO> getOrderItembyId(Long orderItemId);
    Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO);
    Response<Long> countUniqueCustomers();

}
