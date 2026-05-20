package com.shailyverma.feasto.auth_users.entity;

import com.shailyverma.feasto.cart.entity.Cart;
import com.shailyverma.feasto.order.entity.Order;
import com.shailyverma.feasto.payment.entity.Payment;
import com.shailyverma.feasto.review.entity.Review;
import com.shailyverma.feasto.role.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(unique = true)
    private String email;

    @NotNull(message = "password is required")
    private String password;

    private String phoneNumber;

    private String profileUrl;

    private String address;

    private boolean isActive;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="user_roles",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private List<Role> roles;

    @OneToMany(mappedBy = "user" , cascade=CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "user" , cascade=CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(mappedBy = "user" , cascade=CascadeType.ALL)
    private List<Payment> payments;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;







}
