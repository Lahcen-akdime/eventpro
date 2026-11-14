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
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientServiceTest {

    @Mock
    private ClientJpaRepository clientJpaRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService; // le vrai service avec les faux repository / encoder / mapper dedans

    private final UUID id = UUID.randomUUID();

    // ---------- petits helpers (comme OneUser() avant) ----------
    private ClientRegisterRequest OneRegisterRequest(String email, String password) {
        ClientRegisterRequest r = mock(ClientRegisterRequest.class);
        when(r.email()).thenReturn(email);
        when(r.password()).thenReturn(password);
        return r;
    }

    private ClientUpdateRequest OneUpdateRequest(String name, String email, String phone) {
        ClientUpdateRequest r = mock(ClientUpdateRequest.class);
        when(r.name()).thenReturn(name);
        when(r.email()).thenReturn(email);
        when(r.phone()).thenReturn(phone);
        return r;
    }

    private Client OneClient(boolean active) {
        Client c = mock(Client.class);
        when(c.getIsActive()).thenReturn(active);
        return c;
    }

    // =====================================================================
    // save
    // =====================================================================
    @Test
    void save_valide_metLEmailEnMinuscules_hashLePassword_etDonneLeRoleClient() {
        // ARRANGE
        ClientRegisterRequest request = OneRegisterRequest("CLIENT@Gmail.COM", "12345678");
        Client client = mock(Client.class);
        Client saved = mock(Client.class);
        ClientResponse response = mock(ClientResponse.class);
        when(userJpaRepository.existsByEmailIgnoreCase("client@gmail.com")).thenReturn(false);
        when(clientMapper.toEntity(request)).thenReturn(client);
        when(passwordEncoder.encode("12345678")).thenReturn("hash123");
        when(clientJpaRepository.save(client)).thenReturn(saved);
        when(clientMapper.toDtoResponse(saved)).thenReturn(response);

        // ACT
        ClientResponse result = clientService.save(request);

        // ASSERT
        assertSame(response, result);
        verify(client).setEmail("client@gmail.com"); // email en minuscules
        verify(client).setPassword("hash123");       // jamais le password en clair
        verify(client).setRole(Role.CLIENT);
        verify(clientJpaRepository).save(client);
    }

    @Test
    void save_emailDejaUtilise_EmailAlreadyExist_sansRienSauvegarder() {
        ClientRegisterRequest request = OneRegisterRequest("client@gmail.com", "12345678");
        when(userJpaRepository.existsByEmailIgnoreCase("client@gmail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistException.class, () -> clientService.save(request));

        verifyNoInteractions(clientMapper, passwordEncoder);
        verify(clientJpaRepository, never()).save(any());
    }

    // =====================================================================
    // getMyProfile
    // =====================================================================
    @Test
    void getMyProfile_clientTrouve_retourneLeProfil() {
        Client client = mock(Client.class);
        ClientResponse response = mock(ClientResponse.class);
        when(clientJpaRepository.findByEmail("client@gmail.com")).thenReturn(Optional.of(client));
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        ClientResponse result = clientService.getMyProfile("client@gmail.com");

        assertSame(response, result);
    }

    @Test
    void getMyProfile_introuvable_EmailNotFound() {
        when(clientJpaRepository.findByEmail("x@gmail.com")).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> clientService.getMyProfile("x@gmail.com"));

        verifyNoInteractions(clientMapper);
    }

    // =====================================================================
    // getAll
    // =====================================================================
    @Test
    void getAll_retourneLaPageMappee() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10);
        Client c1 = mock(Client.class);
        ClientResponse r1 = mock(ClientResponse.class);
        when(clientJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(c1), pageable, 1));
        when(clientMapper.toDtoResponse(c1)).thenReturn(r1);

        // ACT
        Page<ClientResponse> page = clientService.getAll(pageable);

        // ASSERT
        assertEquals(1, page.getContent().size());
        assertSame(r1, page.getContent().get(0));
    }

    @Test
    void getAll_aucunClient_pageVide() {
        Pageable pageable = PageRequest.of(0, 10);
        when(clientJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ClientResponse> page = clientService.getAll(pageable);

        assertTrue(page.getContent().isEmpty());
        verifyNoInteractions(clientMapper);
    }

    // =====================================================================
    // updateMyProfile
    // =====================================================================
    @Test
    void updateMyProfile_valide_metAJourNomEmailTelephone_etSauvegarde() {
        // ARRANGE
        Client client = mock(Client.class);
        Client entiteMiseAJour = mock(Client.class); // ce que retourne clientMapper.toUpdateEntity(...)
        ClientResponse response = mock(ClientResponse.class);
        ClientUpdateRequest request = OneUpdateRequest("Nouveau Nom", "new@gmail.com", "0612345678");
        when(clientJpaRepository.findByEmail("client@gmail.com")).thenReturn(Optional.of(client));
        when(clientMapper.toUpdateEntity(request)).thenReturn(entiteMiseAJour);
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        // ACT
        ClientResponse result = clientService.updateMyProfile("client@gmail.com", request);

        // ASSERT
        assertSame(response, result);
        verify(client).setName("Nouveau Nom");
        verify(client).setEmail("new@gmail.com");
        verify(client).setPhone("0612345678");
        verify(userJpaRepository).save(entiteMiseAJour);
    }

    @Test
    void updateMyProfile_introuvable_EmailNotFound_sansSauvegarder() {
        ClientUpdateRequest request = OneUpdateRequest("Nom", "new@gmail.com", "0612345678");
        when(clientJpaRepository.findByEmail("x@gmail.com")).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> clientService.updateMyProfile("x@gmail.com", request));

        verify(userJpaRepository, never()).save(any());
    }

    // =====================================================================
    // activate
    // =====================================================================
    @Test
    void activate_clientDesactive_leActiveEtSauvegarde() {
        // ARRANGE
        Client client = OneClient(false);
        ClientResponse response = mock(ClientResponse.class);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        // ACT
        ClientResponse result = clientService.activate(id);

        // ASSERT
        assertSame(response, result);
        verify(client).setIsActive(true);
        verify(userJpaRepository).save(client);
    }

    @Test
    void activate_dejaActif_InvalidOperation_sansSauvegarder() {
        Client client = OneClient(true);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));

        assertThrows(InvalidOperationException.class, () -> clientService.activate(id));

        verify(client, never()).setIsActive(anyBoolean());
        verify(userJpaRepository, never()).save(any());
    }

    @Test
    void activate_introuvable_EmailNotFound() {
        when(clientJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> clientService.activate(id));

        verify(userJpaRepository, never()).save(any());
    }

    // =====================================================================
    // desactivate
    // =====================================================================
    @Test
    void desactivate_clientActif_leDesactiveEtSauvegarde() {
        // ARRANGE
        Client client = OneClient(true);
        ClientResponse response = mock(ClientResponse.class);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientMapper.toDtoResponse(client)).thenReturn(response);

        // ACT
        ClientResponse result = clientService.desactivate(id);

        // ASSERT
        assertSame(response, result);
        verify(client).setIsActive(false);
        verify(userJpaRepository).save(client);
    }

    @Test
    void desactivate_dejaDesactive_InvalidOperation_sansSauvegarder() {
        Client client = OneClient(false);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));

        assertThrows(InvalidOperationException.class, () -> clientService.desactivate(id));

        verify(client, never()).setIsActive(anyBoolean());
        verify(userJpaRepository, never()).save(any());
    }

    @Test
    void desactivate_introuvable_EmailNotFound() {
        when(clientJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> clientService.desactivate(id));

        verify(userJpaRepository, never()).save(any());
    }

    // =====================================================================
    // findByEmail / findById
    // =====================================================================
    @Test
    void findByEmail_trouve_retourneLeClient() {
        Client client = mock(Client.class);
        when(clientJpaRepository.findByEmail("client@gmail.com")).thenReturn(Optional.of(client));

        assertSame(client, clientService.findByEmail("client@gmail.com"));
    }

    @Test
    void findByEmail_introuvable_EmailNotFound() {
        when(clientJpaRepository.findByEmail("x@gmail.com")).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> clientService.findByEmail("x@gmail.com"));
    }

    @Test
    void findById_trouve_retourneLeClient() {
        Client client = mock(Client.class);
        when(clientJpaRepository.findById(id)).thenReturn(Optional.of(client));

        assertSame(client, clientService.findById(id));
    }

    @Test
    void findById_introuvable_EmailNotFound() {
        when(clientJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> clientService.findById(id));
    }

    // =====================================================================
    // getAuthaunticatedClient
    // userService.getAuthenticatedEmail(...) est STATIQUE : meme sur le mock userService, c'est la vraie
    // methode qui s'execute. On lui donne donc une vraie session (mock de HttpSession) dans la requete.
    // =====================================================================
    @Test
    void getAuthaunticatedClient_sessionValide_retourneLeClientConnecte() {
        // ARRANGE
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        Client client = mock(Client.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("authEmail")).thenReturn("client@gmail.com");
        when(clientJpaRepository.findByEmail("client@gmail.com")).thenReturn(Optional.of(client));

        // ACT
        Client result = clientService.getAuthaunticatedClient(request);

        // ASSERT
        assertSame(client, result);
    }

    @Test
    void getAuthaunticatedClient_emailDeSessionSansClient_EmailNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("authEmail")).thenReturn("x@gmail.com");
        when(clientJpaRepository.findByEmail("x@gmail.com")).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> clientService.getAuthaunticatedClient(request));
    }

    @Test
    void getAuthaunticatedClient_pasConnecte_401_sansChercherLeClient() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null); // aucune session

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> clientService.getAuthaunticatedClient(request));

        assertEquals(401, ex.getStatusCode().value());
        verifyNoInteractions(clientJpaRepository);
    }
}