package com.eventPro.api.Service;

import com.eventPro.api.Dto.User.UserDto;
import com.eventPro.api.Entity.User;
import com.eventPro.api.Exception.AccessDeniedException;
import com.eventPro.api.Exception.InvalidCredantialsException;
import com.eventPro.api.Mapper.UserMapper;
import com.eventPro.api.Repository.UserJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder ;
    private final UserMapper userMapper ;
    private static String authEmail  ;

    public Optional<User> findByEmail(String email){
        return userJpaRepository.findByEmail(email) ;
    }

    public User update(User user){
        return userJpaRepository.save(user) ;
    }

    public User save(User user){
        return userJpaRepository.save(user) ;
    }

    public UserDto login(String email , String password){
        User user = userJpaRepository.findByEmail(email.toLowerCase(Locale.ROOT)).orElseThrow(()-> new InvalidCredantialsException("L'email ou le password est incorrect"));
            if(passwordEncoder.matches(password,user.getPassword())){
                return userMapper.toDtoResponse(user) ;
            }
            else {
                throw new InvalidCredantialsException("L'email ou le password est incorrect") ;
            }
        }

    public static String getAuthenticatedEmail(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("authEmail") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, " connecter s'il vous plis");
        }

        authEmail = (String) session.getAttribute("authEmail");
        return authEmail ;
    }

    public static void saveEmailInSession(String email,HttpServletRequest servletRequest){
        HttpSession session = servletRequest.getSession() ;
        session.setAttribute("authEmail",email);
    }

    public void checkIsActive(){
        var user = userJpaRepository.findByEmailIgnoreCase(authEmail) ;
        if(!user.get().getIsActive()){
            throw new AccessDeniedException("Your account is desactivated") ;
        }
    }
}
