package com.eventPro.api.Controller;

import com.eventPro.api.Dto.User.UserDto;
import com.eventPro.api.Dto.User.UserLoginRequest;
import com.eventPro.api.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService ;


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserDto login(@RequestBody UserLoginRequest request, HttpServletRequest servletRequest){
        UserDto userDto = userService.login(request.email(),request.password()) ;
        UserService.saveEmailInSession(request.email(),servletRequest);
        return userDto ;
    }



}
