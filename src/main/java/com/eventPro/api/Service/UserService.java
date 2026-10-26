package com.eventPro.api.Service;

import com.eventPro.api.Entity.User;
import com.eventPro.api.Repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;

    public Optional<User> findByEmail(String email){
        return userJpaRepository.findByEmail(email) ;
    }

    public User update(User user){
        return userJpaRepository.save(user) ;
    }

    public User save(User user){
        return userJpaRepository.save(user) ;
    }



}
