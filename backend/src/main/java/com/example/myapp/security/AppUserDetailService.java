package com.example.myapp.security;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.myapp.entity.AppUser;
import com.example.myapp.repository.AppUserRepository;
@Service
public class AppUserDetailService implements UserDetailsService{
    private final AppUserRepository appUserRepository;
    public AppUserDetailService(AppUserRepository appUserRepository){
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
             AppUser user = appUserRepository.findByEmail(email)
                            .orElseThrow(()->new UsernameNotFoundException("User not found with email: " + email));

            return User.builder().username(user.getEmail())
                       .password(user.getPassword()).roles("USER").build();                
    }
}
