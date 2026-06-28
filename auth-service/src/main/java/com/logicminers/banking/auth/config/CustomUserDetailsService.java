package com.logicminers.banking.auth.config;

import com.logicminers.banking.auth.domain.User;
import com.logicminers.banking.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // We use the strict repository method that ignores deleted users
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in the vault"));

        // Translate our Logic Miners User into a Spring Security User
        // 🛑 THE FIX: Use withUsername() instead of builder()
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .accountLocked(!user.isAccountNonLocked())
                .disabled(!user.isEnabled())
                .build();
    }
}