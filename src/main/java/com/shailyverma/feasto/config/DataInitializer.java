package com.shailyverma.feasto.config;

import com.shailyverma.feasto.role.entity.Role;
import com.shailyverma.feasto.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("CUSTOMER");
        createRoleIfNotExists("DELIVERY");
    }

    private void createRoleIfNotExists(String roleName) {

        if (roleRepository.findByName(roleName).isEmpty()) {

            Role role = new Role();
            role.setName(roleName);

            roleRepository.save(role);

            System.out.println(roleName + " role created.");
        }
    }
}