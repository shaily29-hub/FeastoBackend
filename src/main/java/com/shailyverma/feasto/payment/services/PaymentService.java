    package com.shailyverma.feasto.payment.services;
    
    import com.shailyverma.feasto.exceptions.PaymentProcessingException;
    import com.shailyverma.feasto.payment.dtos.PaymentDTO;
    import com.shailyverma.feasto.response.Response;
    
    import java.util.List;
    
    public interface PaymentService {
    
        Response<?> initializePayment(PaymentDTO paymentDTO);
        void updatePaymentForOrder(PaymentDTO paymentDTO);
        Response<List<PaymentDTO>> getAllPayments();
        Response<PaymentDTO> getPaymentById(Long PaymentId);
    
    }
