package com.waterquality.portal.config;

import com.waterquality.portal.domain.Role;
import com.waterquality.portal.domain.User;
import com.waterquality.portal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Production bootstrap: creates the first admin user from the
 * {@code ADMIN_PASSWORD} environment variable when the user table is empty.
 * No default password exists — without the variable, the app starts with no
 * users (and says so loudly) rather than shipping a well-known credential.
 */
@Configuration
@Profile("prod")
public class ProdBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ProdBootstrap.class);

    @Bean
    CommandLineRunner bootstrapAdmin(UserRepository users,
                                     PasswordEncoder encoder,
                                     @Value("${ADMIN_PASSWORD:}") String adminPassword) {
        return args -> {
            if (users.count() > 0) {
                return;
            }
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("ADMIN_PASSWORD is not set and no users exist: "
                        + "set ADMIN_PASSWORD and restart to create the bootstrap admin user.");
                return;
            }
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode(adminPassword));
            admin.setFullName("System Admin");
            admin.setEmail("admin@waterquality.local");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            users.save(admin);
            log.info("Bootstrap admin user 'admin' created. Change its password after first login.");
        };
    }
}
