package com.example.ecommerce.service;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("লগইন চেষ্টা – username: '" + username + "'");

        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            System.out.println("ইউজার পাওয়া যায়নি: '" + username + "'");
            throw new UsernameNotFoundException("User not found: " + username);
        }

        System.out.println("ইউজার পাওয়া গেছে: " + user.getUsername());
        System.out.println("Hashed password from DB: " + user.getPassword());
        System.out.println("Enabled: " + user.isEnabled());
        System.out.println("Account non-expired: " + user.isAccountNonExpired());
        System.out.println("Account non-locked: " + user.isAccountNonLocked());
        System.out.println("Credentials non-expired: " + user.isCredentialsNonExpired());

        if (!user.isEnabled()) {
            System.out.println("ইউজার disabled আছে");
            throw new UsernameNotFoundException("User is disabled");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")  // পরে role থেকে নাও যদি থাকে
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }
}