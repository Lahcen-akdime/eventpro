package com.eventPro.api;

import com.eventPro.api.Dto.Client.ClientRegisterRequest;
import com.eventPro.api.Dto.Client.ClientResponse;
import com.eventPro.api.Dto.Client.ClientUpdateRequest;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.Role;
import com.eventPro.api.Exception.EmailAlreadyExistException;
import com.eventPro.api.Exception.EmailNotFoundException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Mapper.ClientMapper;
import com.eventPro.api.Repository.ClientJpaRepository;
import com.eventPro.api.Repository.UserJpaRepository;
import com.eventPro.api.Service.ClientService;
import com.eventPro.api.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    UserJpaRepository userJpaRepository;

    @Mock
    ClientJpaRepository clientJpaRepository;

    @Mock
    ClientMapper clientMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserService userService;

    @InjectMocks
    ClientService clientService;


    @Test
    public void emailAreadyExistsTest() {
        String email = "lahcen@gmail.com";
        ClientRegisterRequest client = ClientRegisterRequest.builder()
                .email(email)
                .build();
        when(userJpaRepository.existsByEmailIgnoreCase(email)).thenReturn(true);
        assertThatThrownBy(() -> clientService.save(client)).isInstanceOf(EmailAlreadyExistException.class);
        verify(clientJpaRepository, never()).save(any());
    }

    @Test
    public void saveSuccessTest() {
        ClientRegisterRequest request = ClientRegisterRequest.builder()
                .email("Lahcen@Gmail.COM")
                .password("secret123")
                .build();
        Client entity = new Client();
        ClientResponse response = mock(ClientResponse.class);

        when(userJpaRepository.existsByEmailIgnoreCase("lahcen@gmail.com")).thenReturn(false);
        when(clientMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(clientJpaRepository.save(any(Client.class))).thenReturn(entity);
        when(clientMapper.toDtoResponse(entity)).thenReturn(response);

        ClientResponse result = clientService.save(request);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientJpaRepository).save(captor.capture());
        Client saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("lahcen@gmail.com");
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.CLIENT);
        assertThat(result).isSameAs(response);
    }


    @Test
    public void getMyProfileSuccessTest() {
        Client client = new Client();
        ClientResponse response = mock(ClientResponse.class);
        when(clientJpaRepository.findByEmail("lahcen@gmail.com")).thenReturn(Optional.of(client));
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        ClientResponse result = clientService.getMyProfile("lahcen@gmail.com");

        assertThat(result).isSameAs(response);
    }

    @Test
    public void getMyProfileEmailNotFoundTest() {
        when(clientJpaRepository.findByEmail("inconnu@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getMyProfile("inconnu@gmail.com"))
                .isInstanceOf(EmailNotFoundException.class);
        verify(clientMapper, never()).toDtoResponse(any());
    }


    @Test
    public void getAllTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Client client = new Client();
        ClientResponse response = mock(ClientResponse.class);
        when(clientJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(client)));
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        Page<ClientResponse> result = clientService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }


    @Test
    public void updateMyProfileSuccessTest() {
        ClientUpdateRequest request = mock(ClientUpdateRequest.class);
        when(request.name()).thenReturn("Nouveau Nom");
        when(request.email()).thenReturn("nouveau@gmail.com");
        when(request.phone()).thenReturn("0600000000");

        Client client = new Client();
        ClientResponse response = mock(ClientResponse.class);
        when(clientJpaRepository.findByEmail("lahcen@gmail.com")).thenReturn(Optional.of(client));
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        ClientResponse result = clientService.updateMyProfile("lahcen@gmail.com", request);

        assertThat(client.getName()).isEqualTo("Nouveau Nom");
        assertThat(client.getEmail()).isEqualTo("nouveau@gmail.com");
        assertThat(client.getPhone()).isEqualTo("0600000000");
        verify(userJpaRepository).save(any());
        assertThat(result).isSameAs(response);
    }

    @Test
    public void updateMyProfileEmailNotFoundTest() {
        ClientUpdateRequest request = mock(ClientUpdateRequest.class);
        when(clientJpaRepository.findByEmail("inconnu@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.updateMyProfile("inconnu@gmail.com", request))
                .isInstanceOf(EmailNotFoundException.class);
        verify(userJpaRepository, never()).save(any());
    }


    @Test
    public void activateSuccessTest() {
        UUID id = UUID.randomUUID();
        Client client = new Client();
        client.setIsActive(false);
        ClientResponse response = mock(ClientResponse.class);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        ClientResponse result = clientService.activate(id);

        assertThat(client.getIsActive()).isTrue();
        verify(userJpaRepository).save(client);
        assertThat(result).isSameAs(response);
    }

    @Test
    public void activateAlreadyActiveTest() {
        UUID id = UUID.randomUUID();
        Client client = new Client();
        client.setIsActive(true);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> clientService.activate(id))
                .isInstanceOf(InvalidOperationException.class);
        verify(userJpaRepository, never()).save(any());
    }


    @Test
    public void desactivateSuccessTest() {
        UUID id = UUID.randomUUID();
        Client client = new Client();
        client.setIsActive(true);
        ClientResponse response = mock(ClientResponse.class);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        ClientResponse result = clientService.desactivate(id);

        assertThat(client.getIsActive()).isFalse();
        verify(userJpaRepository).save(client);
        assertThat(result).isSameAs(response);
    }

    @Test
    public void desactivateAlreadyDesactivatedTest() {
        UUID id = UUID.randomUUID();
        Client client = new Client();
        client.setIsActive(false);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> clientService.desactivate(id))
                .isInstanceOf(InvalidOperationException.class);
        verify(userJpaRepository, never()).save(any());
    }


    @Test
    public void findByIdNotFoundTest() {
        UUID id = UUID.randomUUID();
        when(clientJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(id))
                .isInstanceOf(EmailNotFoundException.class);
    }

    @Test
    public void findByIdSuccessTest() {
        UUID id = UUID.randomUUID();
        Client client = new Client();
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));

        assertThat(clientService.findById(id)).isSameAs(client);
    }

    @Test
    public void getAuthenticatedClientSuccessTest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Client client = new Client();
        when(clientJpaRepository.findByEmail("client@gmail.com")).thenReturn(Optional.of(client));

        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> UserService.getAuthenticatedEmail(request)).thenReturn("client@gmail.com");

            assertThat(clientService.getAuthaunticatedClient(request)).isSameAs(client);
        }
    }

    @Test
    public void getAuthenticatedClientNotFoundTest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(clientJpaRepository.findByEmail("inconnu@gmail.com")).thenReturn(Optional.empty());

        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> UserService.getAuthenticatedEmail(request)).thenReturn("inconnu@gmail.com");

            assertThatThrownBy(() -> clientService.getAuthaunticatedClient(request))
                    .isInstanceOf(EmailNotFoundException.class);
        }
    }
}