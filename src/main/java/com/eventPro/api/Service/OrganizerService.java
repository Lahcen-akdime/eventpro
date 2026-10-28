package com.eventPro.api.Service;

import com.eventPro.api.Dto.Organizer.OrganizerRegisterRequest;
import com.eventPro.api.Dto.Organizer.OrganizerResponse;
import com.eventPro.api.Dto.Organizer.OrganizerUpdateRequest;
import com.eventPro.api.Entity.Enum.Role;
import com.eventPro.api.Entity.Organizer;
import com.eventPro.api.Exception.EmailNotFoundException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.UserNotLoggedInException;
import com.eventPro.api.Mapper.OrganizerMapper;
import com.eventPro.api.Repository.OrganizerJpaRepository;
import com.eventPro.api.Repository.UserJpaRepository;
import com.eventPro.api.Exception.EmailAlreadyExistException;
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
public class OrganizerService {

    private final OrganizerJpaRepository organizerJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizerMapper organizerMapper;

    @Transactional
    public OrganizerResponse save(OrganizerRegisterRequest request) {

        String email = request.email().toLowerCase(Locale.ROOT);

        if (userJpaRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistException("Cet email appartient deja à un utilisateur");
        }

        Organizer organizer = organizerMapper.toEntity(request);

        organizer.setEmail(email);
        organizer.setPassword(passwordEncoder.encode(request.password()));
        organizer.setRole(Role.ORGANISER);

        Organizer saved = organizerJpaRepository.save(organizer);

        return organizerMapper.toDtoResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrganizerResponse getMyProfile(String email) {

        Organizer organizer = organizerJpaRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotLoggedInException("Organisateur non connecte"));

        return organizerMapper.toDtoResponse(organizer);
    }

    @Transactional(readOnly = true)
    public Page<OrganizerResponse> getAll(Pageable pageable) {

        return organizerJpaRepository.findAll(pageable)
                                    .map(organizerMapper::toDtoResponse) ;
    }

    @Transactional
    public OrganizerResponse updateMyProfile(String currentEmail , OrganizerUpdateRequest request) {

        Organizer organizer = organizerJpaRepository
                .findByEmailIgnoreCase(currentEmail)
                .orElseThrow(() -> new EmailNotFoundException("organisateur introuvable"));

        organizer.setName(request.name());
        organizer.setEmail(request.email());
        organizer.setOrganizationName(request.organizationName());
        Organizer updated = organizerJpaRepository.save(organizer);
        return organizerMapper.toDtoResponse(updated);
    }

    public OrganizerResponse desactivate(UUID id){
        Organizer organizer = findById(id) ;
        if (!organizer.getIsActive()){
            throw new InvalidOperationException("The client is already desactivated") ;
        }
        organizer.setIsActive(false);
        userJpaRepository.save(organizer) ;
        return organizerMapper.toDtoResponse(organizer) ;
    }

    public OrganizerResponse activate(UUID id){
        Organizer organizer = findById(id) ;
        if (organizer.getIsActive()){
            throw new InvalidOperationException("The client is already activated") ;
        }
        organizer.setIsActive(true);
        userJpaRepository.save(organizer) ;
        return organizerMapper.toDtoResponse(organizer) ;
    }

    public Organizer findByEmail(String email){
        Optional<Organizer> organizer = organizerJpaRepository.findByEmailIgnoreCase(email) ;
        if(!organizer.isPresent()){
            throw new EmailNotFoundException("This email is not found");
        }
        return organizer.get() ;
    }

    public Organizer findById(UUID id){
        Optional<Organizer> organizer = organizerJpaRepository.findById(id) ;
        if(!organizer.isPresent()){
            throw new EmailNotFoundException("The organizer is not found");
        }
        return organizer.get() ;
    }
}