package com.eventPro.api.Service.DataInitializer;

import com.eventPro.api.Entity.Admin;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Entity.Enum.Role;
import com.eventPro.api.Entity.User;
import com.eventPro.api.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminInitializerService {

    private final UserService userService ;
    private final PasswordEncoder passwordEncoder ;

    @Transactional
    public void createOneAdmin(String name, String email, String password){

        Optional<User> user = userService.findByEmail(email) ;
        if(user.isPresent()){
            if(user.get() instanceof Admin){
                return;
            }
            throw new InvalidVDataException("L'email est deja appartient a un autre utilisateur ! ");
        }

        Admin admin = Admin.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build() ;

        userService.save(admin);

    }
}
