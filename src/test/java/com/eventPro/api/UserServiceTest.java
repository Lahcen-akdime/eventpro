package com.eventPro.api;

import com.eventPro.api.Dto.User.UserDto;
import com.eventPro.api.Entity.User;
import com.eventPro.api.Exception.AccessDeniedException;
import com.eventPro.api.Exception.InvalidCredantialsException;
import com.eventPro.api.Mapper.UserMapper;
import com.eventPro.api.Repository.UserJpaRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService; // le vrai service avec les faux repository / encoder / mapper dedans

    // ---------- petits helpers (comme OneUser() avant) ----------
    private User OneUser(String password, boolean active) {
        User u = mock(User.class);
        when(u.getPassword()).thenReturn(password);
        when(u.getIsActive()).thenReturn(active);
        return u;
    }

    // =====================================================================
    // findByEmail / update / save
    // =====================================================================
    @Test
    void findByEmail_utilisateurTrouve_retourneLOptionalRempli() {
        // ARRANGE
        User user = mock(User.class);
        when(userJpaRepository.findByEmail("a@gmail.com")).thenReturn(Optional.of(user));

        // ACT
        Optional<User> result = userService.findByEmail("a@gmail.com");

        // ASSERT
        assertTrue(result.isPresent());
        assertSame(user, result.get());
    }

    @Test
    void findByEmail_introuvable_retourneUnOptionalVide() {
        when(userJpaRepository.findByEmail("x@gmail.com")).thenReturn(Optional.empty());

        assertTrue(userService.findByEmail("x@gmail.com").isEmpty());
    }

    @Test
    void update_sauvegardeEtRetourneLUtilisateur() {
        User user = mock(User.class);
        when(userJpaRepository.save(user)).thenReturn(user);

        User result = userService.update(user);

        assertSame(user, result);
        verify(userJpaRepository).save(user);
    }

    @Test
    void save_sauvegardeEtRetourneLUtilisateur() {
        User user = mock(User.class);
        when(userJpaRepository.save(user)).thenReturn(user);

        User result = userService.save(user);

        assertSame(user, result);
        verify(userJpaRepository).save(user);
    }

    // =====================================================================
    // login
    // =====================================================================
    @Test
    void login_identifiantsCorrects_retourneLeDto() {
        // ARRANGE
        User user = OneUser("hash123", true);
        UserDto dto = mock(UserDto.class);
        when(userJpaRepository.findByEmail("a@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("12345678", "hash123")).thenReturn(true);
        when(userMapper.toDtoResponse(user)).thenReturn(dto);

        // ACT
        UserDto result = userService.login("a@gmail.com", "12345678");

        // ASSERT
        assertSame(dto, result);
        verify(passwordEncoder).matches("12345678", "hash123");
    }

    @Test
    void login_emailEnMajuscules_estMisEnMinusculesAvantLaRecherche() {
        User user = OneUser("hash123", true);
        UserDto dto = mock(UserDto.class);
        when(userJpaRepository.findByEmail("a@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("12345678", "hash123")).thenReturn(true);
        when(userMapper.toDtoResponse(user)).thenReturn(dto);

        UserDto result = userService.login("A@GMAIL.COM", "12345678");

        assertSame(dto, result);
        verify(userJpaRepository).findByEmail("a@gmail.com");
    }

    @Test
    void login_emailInconnu_InvalidCredentials_sansVerifierLePassword() {
        when(userJpaRepository.findByEmail("x@gmail.com")).thenReturn(Optional.empty());

        InvalidCredantialsException ex = assertThrows(InvalidCredantialsException.class,
                () -> userService.login("x@gmail.com", "12345678"));

        assertTrue(ex.getMessage().contains("incorrect"));
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    void login_mauvaisPassword_InvalidCredentials_sansMapper() {
        User user = OneUser("hash123", true);
        when(userJpaRepository.findByEmail("a@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mauvais", "hash123")).thenReturn(false);

        InvalidCredantialsException ex = assertThrows(InvalidCredantialsException.class,
                () -> userService.login("a@gmail.com", "mauvais"));

        assertTrue(ex.getMessage().contains("incorrect"));
        verifyNoInteractions(userMapper);
    }

    // =====================================================================
    // getAuthenticatedEmail (methode statique)
    // =====================================================================
    @Test
    void getAuthenticatedEmail_aucuneSession_401() {
        // getSession(false) retourne null : l'utilisateur n'est pas connecte
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> UserService.getAuthenticatedEmail(request));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void getAuthenticatedEmail_sessionSansAttributAuthEmail_401() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("authEmail")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> UserService.getAuthenticatedEmail(request));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void getAuthenticatedEmail_sessionAvecEmail_retourneLEmail() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("authEmail")).thenReturn("a@gmail.com");

        String email = UserService.getAuthenticatedEmail(request);

        assertEquals("a@gmail.com", email);
        verify(request).getSession(false); // ne cree jamais de nouvelle session
        verify(request, never()).getSession();
    }

    // =====================================================================
    // saveEmailInSession (methode statique)
    // =====================================================================
    @Test
    void saveEmailInSession_metLEmailDansLaSession() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        UserService.saveEmailInSession("a@gmail.com", request);

        verify(session).setAttribute("authEmail", "a@gmail.com");
    }

    // =====================================================================
    // checkIsActive
    // =====================================================================
    @Test
    void checkIsActive_utilisateurIntrouvable_AccessDenied() {
        when(userJpaRepository.findByEmailIgnoreCase("x@gmail.com")).thenReturn(Optional.empty());

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> userService.checkIsActive("x@gmail.com"));

        assertTrue(ex.getMessage().contains("desactivated"));
    }

    @Test
    void checkIsActive_compteDesactive_AccessDenied() {
        User user = OneUser("hash123", false);
        when(userJpaRepository.findByEmailIgnoreCase("a@gmail.com")).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> userService.checkIsActive("a@gmail.com"));
    }

    @Test
    void checkIsActive_compteActif_neLevePasDException() {
        User user = OneUser("hash123", true);
        when(userJpaRepository.findByEmailIgnoreCase("a@gmail.com")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.checkIsActive("a@gmail.com"));
    }
}