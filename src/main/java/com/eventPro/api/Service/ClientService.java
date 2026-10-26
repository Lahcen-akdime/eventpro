package com.eventPro.api.Service;

import com.eventPro.api.Dto.ClientRegisterRequest;
import com.eventPro.api.Dto.ClientResponse;
import com.eventPro.api.Exception.EmailAlreadyExistException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.Role;
import com.eventPro.api.Entity.User;
import com.eventPro.api.Mapper.ClientMapper;
import com.eventPro.api.Repository.ClientJpaRepository;
import com.eventPro.api.Repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientJpaRepository clientJpaRepository ;
    private  final UserService userService ;
    private final UserJpaRepository userJpaRepository ;
    private final PasswordEncoder passwordEncoder;
    private final ClientMapper clientMapper ;

    @Transactional
    public ClientResponse save(ClientRegisterRequest clientRegisterRequest){
        String email = clientRegisterRequest.email().toLowerCase(Locale.ROOT) ;
        if(userJpaRepository.existsByEmailIgnoreCase(email)){
            throw new EmailAlreadyExistException("L'email est deja appartient a un autre utilisateur ! ");
        }

        Client client = clientMapper.toEntity(clientRegisterRequest) ;
        client.setEmail(email);
        client.setPassword(passwordEncoder.encode(clientRegisterRequest.password()));
        client.setRole(Role.CLIENT) ;

        Client saved = clientJpaRepository.save(client);
        System.out.println("client inscrit : "+saved.getId());
        return clientMapper.toDtoResponse(saved) ;
    }
}
