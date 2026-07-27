package com.vaultscale.config;

import com.vaultscale.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.core.userdetails.*;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    // This bean is used by Spring Security's DaoAuthenticationProvider
    // When login is attempted, Spring calls loadUserByUsername(email)
    // We return the User entity which implements UserDetails
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() ->
                    new UsernameNotFoundException("User not found: " + username)
                );
    }
}
