package com.eventPro.api;

import com.eventPro.api.Entity.Admin;
import com.eventPro.api.Entity.Enum.Role;
import com.eventPro.api.Entity.User;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Service.DataInitializer.AdminInitializerService;
import com.eventPro.api.Service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
class AdminInitializerServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminInitializerService service; // le vrai service avec les faux userService / encoder dedans

    // =====================================================================
    // createOneAdmin
    // =====================================================================
    @Test
    void createOneAdmin_emailLibre_creeEtSauvegardeUnAdminAvecPasswordHashe() {
        // ARRANGE
        when(userService.findByEmail("admin@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin1234")).thenReturn("hash123");

        // ACT
        service.createOneAdmin("Admin", "admin@gmail.com", "admin1234");

        // ASSERT : on recupere l'Admin envoye a userService.save(...)
        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(userService).save(captor.capture());
        Admin saved = captor.getValue();
        assertEquals("Admin", saved.getName());
        assertEquals("admin@gmail.com", saved.getEmail());
        assertEquals("hash123", saved.getPassword()); // jamais le password en clair
        assertEquals(Role.ADMIN, saved.getRole());
    }

    @Test
    void createOneAdmin_adminDejaExistant_neFaitRien() {
        // l'email appartient deja a un Admin : l'initialisation est idempotente (relancer l'appli ne casse rien)
        Admin existant = mock(Admin.class);
        when(userService.findByEmail("admin@gmail.com")).thenReturn(Optional.of(existant));

        assertDoesNotThrow(() -> service.createOneAdmin("Admin", "admin@gmail.com", "admin1234"));

        verify(userService, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createOneAdmin_emailDUnAutreTypeDUtilisateur_InvalidVData_sansRienSauvegarder() {
        // l'email est pris par un utilisateur qui n'est pas Admin (ex: un client)
        User autre = mock(User.class);
        when(userService.findByEmail("admin@gmail.com")).thenReturn(Optional.of(autre));

        assertThrows(InvalidVDataException.class,
                () -> service.createOneAdmin("Admin", "admin@gmail.com", "admin1234"));

        verify(userService, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }
}