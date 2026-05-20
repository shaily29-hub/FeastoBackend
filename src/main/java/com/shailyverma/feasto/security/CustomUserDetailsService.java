package com.shailyverma.feasto.security;

import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.auth_users.repository.UserRepository;
import com.shailyverma.feasto.exceptions.NotFoundException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user=userRepository.findByEmail(username)
                .orElseThrow(()->new NotFoundException("user not found"));

        return AuthUser.builder()
                .user(user)
                .build();
    }
}
