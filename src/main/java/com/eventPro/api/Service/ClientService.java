package com.eventPro.api.Service;

import com.eventPro.api.Dto.Client.ClientRegisterRequest;
import com.eventPro.api.Dto.Client.ClientResponse;
import com.eventPro.api.Dto.Client.ClientUpdateRequest;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Exception.EmailAlreadyExistException;
import com.eventPro.api.Exception.EmailNotFoundException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Entity.Enum.Role;
import com.eventPro.api.Mapper.ClientMapper;
import com.eventPro.api.Repository.ClientJpaRepository;
import com.eventPro.api.Repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

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
            throw new EmailAlreadyExistException("email est deja appartient a un utilisateur ");
        }

        Client client = clientMapper.toEntity(clientRegisterRequest) ;
        client.setEmail(email);
        client.setPassword(passwordEncoder.encode(clientRegisterRequest.password()));
        client.setRole(Role.CLIENT) ;

        Client saved = clientJpaRepository.save(client);
        System.out.println("client inscrit : "+saved.getId());
        return clientMapper.toDtoResponse(saved) ;
    }

    public ClientResponse getMyProfile(String email){
        Client client = findByEmail(email) ;
        return clientMapper.toDtoResponse(client) ;
    }

    public Page<ClientResponse> getAll(Pageable pageable){
         return clientJpaRepository.findAll(pageable)
                                    .map(clientMapper::toDtoResponse) ;
    }

    public ClientResponse updateMyProfile(String currentEmail , ClientUpdateRequest request){
        Client client = findByEmail(currentEmail) ;
        client.setName(request.name()) ;
        client.setEmail(request.email()); ;
        client.setPhone(request.phone()); ;
        userJpaRepository.save(clientMapper.toUpdateEntity(request)) ;
        return clientMapper.toDtoResponse(client) ;
    }


    public ClientResponse activate(UUID id){
        Client client = findById(id) ;
        if (client.getIsActive()){
            throw new InvalidOperationException("The client is already activated") ;
        }
        client.setIsActive(true);
        userJpaRepository.save(client) ;
        return clientMapper.toDtoResponse(client) ;
    }

    public ClientResponse desactivate(UUID id){
        Client client = findById(id) ;
        if (!client.getIsActive()){
            throw new InvalidOperationException("The client is already desactivated") ;
        }
        client.setIsActive(false);
        userJpaRepository.save(client) ;
        return clientMapper.toDtoResponse(client) ;
    }

    public Client findByEmail(String email){
        Optional<Client> client = clientJpaRepository.findByEmail(email) ;
        if(!client.isPresent()){
            throw new EmailNotFoundException("This email is not found");
        }
        return client.get() ;
    }

    public Client findById(UUID id){
        Optional<Client> client = clientJpaRepository.findById(id) ;
        if(!client.isPresent()){
            throw new EmailNotFoundException("The client is not found");
        }
        return client.get() ;
    }

}
