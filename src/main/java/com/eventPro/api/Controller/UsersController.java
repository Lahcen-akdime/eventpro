package com.eventPro.api.Controller;

import com.eventPro.api.Dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UsersController {

    @PutMapping("/me")
    public Boolean UpdateProfile(@RequestBody UserDto user){
        return true ;
    }


}
