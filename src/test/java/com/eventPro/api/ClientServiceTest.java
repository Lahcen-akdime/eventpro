package com.eventPro.api;


import com.eventPro.api.Dto.Client.ClientRegisterRequest;
import com.eventPro.api.Exception.EmailAlreadyExistException;
import com.eventPro.api.Mapper.ClientMapper;
import com.eventPro.api.Repository.ClientJpaRepository;
import com.eventPro.api.Repository.UserJpaRepository;
import com.eventPro.api.Service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    UserJpaRepository userJpaRepository ;

    @Mock
    ClientJpaRepository clientJpaRepository ;

    @Mock
    ClientMapper clientMapper ;

    @Mock
    PasswordEncoder passwordEncoder ;

    @InjectMocks
    ClientService clientService ;

    @Test
    public void emailAreadyExistsTest(){
        String email = "lahcen@gmail.com" ;
        ClientRegisterRequest client = ClientRegisterRequest.builder()
                            .email(email)
                            .build();
        when(userJpaRepository.existsByEmailIgnoreCase(email)).thenReturn(true) ;
        assertThatThrownBy(()->clientService.save(client)).isInstanceOf(EmailAlreadyExistException.class) ;
        verify(clientJpaRepository,never()).save(any()) ;
    }

}
