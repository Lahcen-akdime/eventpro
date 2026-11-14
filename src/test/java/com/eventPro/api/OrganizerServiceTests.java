package com.eventPro.api;

import com.eventPro.api.Dto.Organizer.OrganizerRegisterRequest;
import com.eventPro.api.Dto.Organizer.OrganizerResponse;
import com.eventPro.api.Dto.Organizer.OrganizerUpdateRequest;
import com.eventPro.api.Entity.Enum.Role;
import com.eventPro.api.Entity.Organizer;
import com.eventPro.api.Exception.EmailAlreadyExistException;
import com.eventPro.api.Exception.EmailNotFoundException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.UserNotLoggedInException;
import com.eventPro.api.Mapper.OrganizerMapper;
import com.eventPro.api.Repository.OrganizerJpaRepository;
import com.eventPro.api.Repository.UserJpaRepository;
import com.eventPro.api.Service.OrganizerService;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
@MockitoSettings(strictness = Strictness.LENIENT)
class OrganizerServiceTests {

    @Mock
    private OrganizerJpaRepository organizerJpaRepository;
    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OrganizerMapper organizerMapper;

    @InjectMocks
    private OrganizerService organizerService;

    private final UUID id = UUID.randomUUID();

    private OrganizerRegisterRequest OneRegisterRequest(String email, String password) {
        OrganizerRegisterRequest r = mock(OrganizerRegisterRequest.class);
        when(r.email()).thenReturn(email);
        when(r.password()).thenReturn(password);
        return r;
    }

    private OrganizerUpdateRequest OneUpdateRequest(String name, String email, String organizationName) {
        OrganizerUpdateRequest r = mock(OrganizerUpdateRequest.class);
        when(r.name()).thenReturn(name);
        when(r.email()).thenReturn(email);
        when(r.organizationName()).thenReturn(organizationName);
        return r;
    }

    private Organizer OneOrganizer(boolean active) {
        Organizer o = mock(Organizer.class);
        when(o.getIsActive()).thenReturn(active);
        return o;
    }

    // =====================================================================
    // save
    // =====================================================================
    @Test
    void save_valide_metLEmailEnMinuscules_hashLePassword_etDonneLeRoleOrganiser() {
        // ARRANGE
        OrganizerRegisterRequest request = OneRegisterRequest("ORGA@Gmail.COM", "12345678");
        Organizer organizer = mock(Organizer.class);
        Organizer saved = mock(Organizer.class);
        OrganizerResponse response = mock(OrganizerResponse.class);
        when(userJpaRepository.existsByEmailIgnoreCase("orga@gmail.com")).thenReturn(false);
        when(organizerMapper.toEntity(request)).thenReturn(organizer);
        when(passwordEncoder.encode("12345678")).thenReturn("hash123");
        when(organizerJpaRepository.save(organizer)).thenReturn(saved);
        when(organizerMapper.toDtoResponse(saved)).thenReturn(response);

        // ACT
        OrganizerResponse result = organizerService.save(request);

        // ASSERT
        assertSame(response, result);
        verify(organizer).setEmail("orga@gmail.com");  // email en minuscules
        verify(organizer).setPassword("hash123");      // jamais le password en clair
        verify(organizer).setRole(Role.ORGANISER);
        verify(organizerJpaRepository).save(organizer);
    }

    @Test
    void save_emailDejaUtilise_EmailAlreadyExist_sansRienSauvegarder() {
        OrganizerRegisterRequest request = OneRegisterRequest("orga@gmail.com", "12345678");
        when(userJpaRepository.existsByEmailIgnoreCase("orga@gmail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistException.class, () -> organizerService.save(request));

        verifyNoInteractions(organizerMapper, passwordEncoder);
        verify(organizerJpaRepository, never()).save(any());
    }

    // =====================================================================
    // getMyProfile
    // =====================================================================
    @Test
    void getMyProfile_organisateurTrouve_retourneLeProfil() {
        Organizer organizer = mock(Organizer.class);
        OrganizerResponse response = mock(OrganizerResponse.class);
        when(organizerJpaRepository.findByEmailIgnoreCase("orga@gmail.com")).thenReturn(Optional.of(organizer));
        when(organizerMapper.toDtoResponse(organizer)).thenReturn(response);

        OrganizerResponse result = organizerService.getMyProfile("orga@gmail.com");

        assertSame(response, result);
    }

    @Test
    void getMyProfile_introuvable_UserNotLoggedIn() {
        when(organizerJpaRepository.findByEmailIgnoreCase("x@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotLoggedInException.class, () -> organizerService.getMyProfile("x@gmail.com"));

        verifyNoInteractions(organizerMapper);
    }

    // =====================================================================
    // getAll
    // =====================================================================
    @Test
    void getAll_retourneLaPageMappee() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10);
        Organizer o1 = mock(Organizer.class);
        OrganizerResponse r1 = mock(OrganizerResponse.class);
        when(organizerJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(o1), pageable, 1));
        when(organizerMapper.toDtoResponse(o1)).thenReturn(r1);

        // ACT
        Page<OrganizerResponse> page = organizerService.getAll(pageable);

        // ASSERT
        assertEquals(1, page.getContent().size());
        assertSame(r1, page.getContent().get(0));
    }

    @Test
    void getAll_aucunOrganisateur_pageVide() {
        Pageable pageable = PageRequest.of(0, 10);
        when(organizerJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<OrganizerResponse> page = organizerService.getAll(pageable);

        assertTrue(page.getContent().isEmpty());
        verifyNoInteractions(organizerMapper);
    }

    // =====================================================================
    // updateMyProfile
    // =====================================================================
    @Test
    void updateMyProfile_valide_metAJourNomEmailEtOrganisation() {
        // ARRANGE
        Organizer organizer = mock(Organizer.class);
        Organizer updated = mock(Organizer.class);
        OrganizerResponse response = mock(OrganizerResponse.class);
        OrganizerUpdateRequest request = OneUpdateRequest("Nouveau Nom", "new@gmail.com", "Nouvelle Orga");
        when(organizerJpaRepository.findByEmailIgnoreCase("orga@gmail.com")).thenReturn(Optional.of(organizer));
        when(organizerJpaRepository.save(organizer)).thenReturn(updated);
        when(organizerMapper.toDtoResponse(updated)).thenReturn(response);

        // ACT
        OrganizerResponse result = organizerService.updateMyProfile("orga@gmail.com", request);

        // ASSERT
        assertSame(response, result);
        verify(organizer).setName("Nouveau Nom");
        verify(organizer).setEmail("new@gmail.com");
        verify(organizer).setOrganizationName("Nouvelle Orga");
        verify(organizerJpaRepository).save(organizer);
    }

    @Test
    void updateMyProfile_introuvable_EmailNotFound_sansSauvegarder() {
        OrganizerUpdateRequest request = OneUpdateRequest("Nom", "new@gmail.com", "Orga");
        when(organizerJpaRepository.findByEmailIgnoreCase("x@gmail.com")).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> organizerService.updateMyProfile("x@gmail.com", request));

        verify(organizerJpaRepository, never()).save(any());
    }

    // =====================================================================
    // desactivate
    // =====================================================================
    @Test
    void desactivate_organisateurActif_ledesactiveEtSauvegarde() {
        // ARRANGE
        Organizer organizer = OneOrganizer(true);
        OrganizerResponse response = mock(OrganizerResponse.class);
        when(organizerJpaRepository.findById(id)).thenReturn(Optional.of(organizer));
        when(organizerMapper.toDtoResponse(organizer)).thenReturn(response);

        // ACT
        OrganizerResponse result = organizerService.desactivate(id);

        // ASSERT
        assertSame(response, result);
        verify(organizer).setIsActive(false);
        verify(userJpaRepository).save(organizer);
    }

    @Test
    void desactivate_dejaDesactive_InvalidOperation_sansSauvegarder() {
        Organizer organizer = OneOrganizer(false);
        when(organizerJpaRepository.findById(id)).thenReturn(Optional.of(organizer));

        assertThrows(InvalidOperationException.class, () -> organizerService.desactivate(id));

        verify(organizer, never()).setIsActive(anyBoolean());
        verify(userJpaRepository, never()).save(any());
    }

    @Test
    void desactivate_introuvable_EmailNotFound() {
        when(organizerJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> organizerService.desactivate(id));

        verify(userJpaRepository, never()).save(any());
    }

    // =====================================================================
    // activate
    // =====================================================================
    @Test
    void activate_organisateurDesactive_leActiveEtSauvegarde() {
        // ARRANGE
        Organizer organizer = OneOrganizer(false);
        OrganizerResponse response = mock(OrganizerResponse.class);
        when(organizerJpaRepository.findById(id)).thenReturn(Optional.of(organizer));
        when(organizerMapper.toDtoResponse(organizer)).thenReturn(response);

        // ACT
        OrganizerResponse result = organizerService.activate(id);

        // ASSERT
        assertSame(response, result);
        verify(organizer).setIsActive(true);
        verify(userJpaRepository).save(organizer);
    }

    @Test
    void activate_dejaActif_InvalidOperation_sansSauvegarder() {
        Organizer organizer = OneOrganizer(true);
        when(organizerJpaRepository.findById(id)).thenReturn(Optional.of(organizer));

        assertThrows(InvalidOperationException.class, () -> organizerService.activate(id));

        verify(organizer, never()).setIsActive(anyBoolean());
        verify(userJpaRepository, never()).save(any());
    }

    @Test
    void activate_introuvable_EmailNotFound() {
        when(organizerJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> organizerService.activate(id));

        verify(userJpaRepository, never()).save(any());
    }

    // =====================================================================
    // findByEmail / findById
    // =====================================================================
    @Test
    void findByEmail_trouve_retourneLOrganisateur() {
        Organizer organizer = mock(Organizer.class);
        when(organizerJpaRepository.findByEmailIgnoreCase("orga@gmail.com")).thenReturn(Optional.of(organizer));

        assertSame(organizer, organizerService.findByEmail("orga@gmail.com"));
    }

    @Test
    void findByEmail_introuvable_EmailNotFound() {
        when(organizerJpaRepository.findByEmailIgnoreCase("x@gmail.com")).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> organizerService.findByEmail("x@gmail.com"));
    }

    @Test
    void findById_trouve_retourneLOrganisateur() {
        Organizer organizer = mock(Organizer.class);
        when(organizerJpaRepository.findById(id)).thenReturn(Optional.of(organizer));

        assertSame(organizer, organizerService.findById(id));
    }

    @Test
    void findById_introuvable_EmailNotFound() {
        when(organizerJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> organizerService.findById(id));
    }
}