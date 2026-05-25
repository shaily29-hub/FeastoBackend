package com.shailyverma.feasto.auth_users.services;

import com.shailyverma.feasto.auth_users.dtos.UserDTO;
import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.auth_users.repository.UserRepository;
import com.shailyverma.feasto.cloudinary.CloudinaryService;
import com.shailyverma.feasto.email_notification.dtos.NotificationDTO;
import com.shailyverma.feasto.email_notification.services.NotificationService;
import com.shailyverma.feasto.exceptions.BadRequestException;
import com.shailyverma.feasto.exceptions.NotFoundException;
import com.shailyverma.feasto.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static java.util.stream.DoubleStream.builder;


@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;

 @Override
public User getCurrentLoggedInUser() {

    Object principal = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

    String email;

    if (principal instanceof UserDetails) {
        email = ((UserDetails) principal).getUsername();
    } else {
        email = principal.toString();
    }

    return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("user not found"));
}

    @Override
    public Response<List<UserDTO>> getAllUsers() {
     log.info("INSIDE getAllUSers()");
        List<User> userList =userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOS=modelMapper.map(userList, new TypeToken<List<UserDTO>>() {}.getType());

        return Response.<List<UserDTO>>builder()
                .statusCode((HttpStatus.OK.value()))
                .message("All users retrieved successfully")
                .data(userDTOS)
                .build();


    }

    @Override
    public Response<UserDTO> getOwnAccountDetails() {
       log.info("INSIDE getownAccountDetails()");
       User user=getCurrentLoggedInUser();

       UserDTO userDTO=modelMapper.map(user,UserDTO.class);

       return Response.<UserDTO>builder()
               .statusCode(HttpStatus.OK.value())
               .message("success")
               .data(userDTO)
               .build();

    }

    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {

        log.info("INSIDE updateOwnAccount()");

        User user = getCurrentLoggedInUser();
        String profileUrl = user.getProfileUrl();
        MultipartFile imageFile = userDTO.getImageFile();

        // 🔥 IMAGE UPDATE
        if (imageFile != null && !imageFile.isEmpty()) {

            if (profileUrl != null && !profileUrl.isEmpty()) {

                String publicId = profileUrl.substring(
                        profileUrl.indexOf("upload/") + 7,
                        profileUrl.lastIndexOf(".")
                );

                cloudinaryService.deleteFile(publicId);
                log.info("Deleted old profile image from Cloudinary");
            }

            String newImageUrl = cloudinaryService.uploadFile(imageFile);
            user.setProfileUrl(newImageUrl);
            System.out.println("Image file: " + imageFile);
        }

        //  BASIC FIELDS
        if (userDTO.getName() != null) user.setName(userDTO.getName());
        if (userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getAddress() != null) user.setAddress(userDTO.getAddress());
        if (userDTO.getPassword() != null)
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // EMAIL FIX
        if(userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {

            if(userRepository.existsByEmail(userDTO.getEmail())) {
                throw new BadRequestException("Email already exists");
            }

            user.setEmail(userDTO.getEmail());   // ✅ inside if
        }

        // SAVE UPDATED USER
        User updatedUser = userRepository.save(user);
        UserDTO dto = modelMapper.map(updatedUser, UserDTO.class);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account updated successfully")
                .data(dto)   // ✅ return updated data
                .build();
    }

    @Override
    public Response<?> deactivateOwnAccount(){
        log.info("INSIDE deactivateAccount()");

        User user=getCurrentLoggedInUser();

        user.setActive(false);
        userRepository.save(user);

        //Send email after deactivation

        NotificationDTO notificationDTO=NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Account Deactivated")
                .body("your account has been deactivated.If this was a mistake, please contact support")
                .build();

        notificationService.sendEmail(notificationDTO);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account deactivated successfully")
                .build();
 }
}
