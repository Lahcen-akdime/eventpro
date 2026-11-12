package com.eventPro.api;

import com.eventPro.api.Dto.User.UserDto;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.Role;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void findsUserByEmail() {
        User user = Client.builder().email("user@example.com").build();
        when(userJpaRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertEquals(Optional.of(user), userService.findByEmail("user@example.com"));
    }

    @Test
    void savesAndUpdatesUser() {
        User user = Client.builder().email("user@example.com").build();
        when(userJpaRepository.save(user)).thenReturn(user);

        assertSame(user, userService.save(user));
        assertSame(user, userService.update(user));
        verify(userJpaRepository, times(2)).save(user);
    }

    @Test
    void logsInWithEmailIgnoringCaseWhenPasswordMatches() {
        User user = Client.builder()
                .name("Amina")
                .email("user@example.com")
                .password("hashed-password")
                .role(Role.CLIENT)
                .build();
        UserDto response = UserDto.builder().name("Amina").email("user@example.com").role(Role.CLIENT).build();
        when(userJpaRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed-password")).thenReturn(true);
        when(userMapper.toDtoResponse(user)).thenReturn(response);

        assertSame(response, userService.login("USER@EXAMPLE.COM", "secret"));
        verify(userMapper).toDtoResponse(user);
    }

    @Test
    void rejectsLoginWhenUserDoesNotExistOrPasswordDoesNotMatch() {
        when(userJpaRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredantialsException.class,
                () -> userService.login("MISSING@example.com", "secret"));

        User user = Client.builder()
                .email("user@example.com")
                .password("hashed-password")
                .build();
        when(userJpaRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredantialsException.class,
                () -> userService.login("user@example.com", "wrong"));
        verify(userMapper, never()).toDtoResponse(any());
    }

    @Test
    void savesEmailInSessionAndReadsItBack() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("authEmail")).thenReturn("user@example.com");

        UserService.saveEmailInSession("user@example.com", request);

        verify(session).setAttribute("authEmail", "user@example.com");
        assertEquals("user@example.com", UserService.getAuthenticatedEmail(request));
    }

    @Test
    void rejectsRequestWithoutAuthenticatedSession() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> UserService.getAuthenticatedEmail(request));

        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("authEmail")).thenReturn(null);
        assertThrows(ResponseStatusException.class, () -> UserService.getAuthenticatedEmail(request));
    }

    @Test
    void permitsActiveUsersAndRejectsMissingOrInactiveUsers() {
        User activeUser = Client.builder().isActive(true).build();
        when(userJpaRepository.findByEmailIgnoreCase("active@example.com")).thenReturn(Optional.of(activeUser));

        assertDoesNotThrow(() -> userService.checkIsActive("active@example.com"));

        when(userJpaRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> userService.checkIsActive("missing@example.com"));

        User inactiveUser = Client.builder().isActive(false).build();
        when(userJpaRepository.findByEmailIgnoreCase("inactive@example.com")).thenReturn(Optional.of(inactiveUser));
        assertThrows(AccessDeniedException.class, () -> userService.checkIsActive("inactive@example.com"));
    }
}
