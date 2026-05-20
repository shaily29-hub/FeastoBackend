package com.shailyverma.feasto.auth_users.services;

import com.shailyverma.feasto.auth_users.dtos.LoginRequest;
import com.shailyverma.feasto.auth_users.dtos.LoginResponse;
import com.shailyverma.feasto.auth_users.dtos.RegistrationRequest;
import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.auth_users.repository.UserRepository;
import com.shailyverma.feasto.exceptions.BadRequestException;
import com.shailyverma.feasto.exceptions.NotFoundException;
import com.shailyverma.feasto.response.Response;
import com.shailyverma.feasto.role.entity.Role;
import com.shailyverma.feasto.role.repository.RoleRepository;
import com.shailyverma.feasto.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.DoubleStream.builder;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;


    @Override
    public Response<?> registration(RegistrationRequest registrationRequest) {
        log.info("INSIDE register()");

        if(userRepository.existsByEmail((registrationRequest.getEmail()))){
            throw new BadRequestException("Email Already Exists");
        }

        List<Role> userRoles;

        if(registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()){
            userRoles=registrationRequest.getRoles().stream()
                    .map(roleName->roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(()->new NotFoundException("Role with name: " + roleName+"Not Found")))
                            .toList();


        }
        else{
            Role defaultRole=roleRepository.findByName("CUSTOMER")
                    .orElseThrow(()->new NotFoundException("Default CUSTOMER role not found"));
            userRoles=List.of(defaultRole);
        }

        User userToSave=User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .address(registrationRequest.getAddress())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(userRoles)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(userToSave);
        log.info("user registered successfully");

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("user registered successfully")
                .build();

    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {

            log.info("INSIDE login()");

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new NotFoundException("Invalid Email"));

            if (!user.isActive()) {
                throw new NotFoundException("Account not active, Please contact customer support");
            }

            // verify the password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new BadRequestException("Invalid Password");
            }

            // Generate a token
            String token = jwtUtils.generateToken(user.getEmail());

            // Extract roles names as a list
            List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .toList();

            // (Return response - not shown in image, but you’ll need something like this)
        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setId(user.getId());

        loginResponse.setToken(token);

        loginResponse.setRoles(roleNames);

       return Response.<LoginResponse>builder()
               .statusCode(HttpStatus.OK.value())
               .message("Login successful")
               .data(loginResponse)
               .build();
        }
    }
